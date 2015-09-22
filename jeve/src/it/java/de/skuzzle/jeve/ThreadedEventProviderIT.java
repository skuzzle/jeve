package de.skuzzle.jeve;

import static org.mockito.Mockito.doAnswer;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import de.skuzzle.jeve.providers.AsynchronousEventProvider;
import de.skuzzle.jeve.providers.BlockingParallelEventProvider;
import de.skuzzle.jeve.providers.ExecutorAware;
import de.skuzzle.jeve.providers.ParallelEventProvider;
import de.skuzzle.jeve.providers.StatisticsEventProvider;
import de.skuzzle.jeve.stores.DefaultListenerStore;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

/**
 * Runs all basic tests for all default provided event providers.
 *
 * @author Simon Taddiken
 */
@RunWith(Parameterized.class)
public class ThreadedEventProviderIT extends EventProviderTestBase {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) ParallelEventProvider::new,
                        (Supplier<ListenerStore>) () -> DefaultListenerStore.create().synchronizedView()
                },
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) AsynchronousEventProvider::new,
                        (Supplier<ListenerStore>) () -> DefaultListenerStore.create().synchronizedView()
                },
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) BlockingParallelEventProvider::new,
                        (Supplier<ListenerStore>) () -> DefaultListenerStore.create().synchronizedView()
                },
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) store-> new StatisticsEventProvider<>(new ParallelEventProvider(store)),
                        (Supplier<ListenerStore>) () -> DefaultListenerStore.create().synchronizedView()
                },
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) store-> new StatisticsEventProvider<>(new BlockingParallelEventProvider(store)),
                        (Supplier<ListenerStore>) () -> DefaultListenerStore.create().synchronizedView()
                },
                new Object[] {
                        (Function<ListenerStore, ? extends EventProvider>) store-> new StatisticsEventProvider<>(new AsynchronousEventProvider(store)),
                        (Supplier<ListenerStore>) () -> DefaultListenerStore.create().synchronizedView()
                });
    }

    /**
     * Creates new BasicEventProviderTests
     *
     * @param factory Factory to create a single provider
     * @param sourceFactory Factory to create a listener store
     */
    public ThreadedEventProviderIT(
            Function<ListenerSource, ? extends EventProvider> factory,
            Supplier<? extends ListenerStore> sourceFactory) {
        super(factory, sourceFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setThreadPoolNull() {
        Assume.assumeTrue(this.subject instanceof ExecutorAware);
            final ExecutorAware ea = (ExecutorAware) this.subject;
            ea.setExecutorService(null);
    }

    @Test
    public void testSetExecutor() {
        Assume.assumeTrue(this.subject instanceof ExecutorAware);
        final ExecutorAware ea = (ExecutorAware) this.subject;
        final ExecutorService executor = Mockito.mock(ExecutorService.class);
        ea.setExecutorService(executor);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Runnable r = (Runnable) invocation.getArguments()[0];
                r.run();
                return null;
            }}).when(executor).execute(Mockito.any());
        final StringListener listener = Mockito.mock(StringListener.class);
        this.store.add(StringListener.class, listener);

        this.subject.dispatch(new StringEvent(this.subject, ""),
                StringListener::onStringEvent);

        Mockito.verify(executor).execute(Mockito.any());
    }
}
