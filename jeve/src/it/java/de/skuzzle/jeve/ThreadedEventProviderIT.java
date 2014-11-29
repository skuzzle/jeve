package de.skuzzle.jeve;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.providers.ExecutorAware;
import de.skuzzle.jeve.stores.DefaultListenerStore;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

/**
 * Runs all basic tests for all default provided event providers.
 *
 * @author Simon Taddiken
 */
@RunWith(Parameterized.class)
public class ThreadedEventProviderIT extends
        EventProviderTestBase<DefaultListenerStore> {

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] { EventProvider.configure().defaultStore().useParallelProvider().createSupplier() },
                new Object[] { EventProvider.configure().defaultStore().useAsynchronousProvider().createSupplier() },
                new Object[] { EventProvider.configure().defaultStore().useParallelProvider().and().statistics().createSupplier() },
                new Object[] { EventProvider.configure().defaultStore().useAsynchronousProvider().and().statistics().createSupplier() }
                );
    }

    /**
     * Creates new BasicEventProviderTests
     *
     * @param factory Factory to create a single provider
     */
    public ThreadedEventProviderIT(
            Supplier<? extends EventProvider<DefaultListenerStore>> factory) {
        super(factory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setThreadPoolNull() {
        if (this.subject instanceof ExecutorAware) {
            final ExecutorAware ea = (ExecutorAware) this.subject;
            ea.setExecutorService(null);
        }
        throw new IllegalArgumentException();
    }

    @Test
    public void testSetExecutor() {
        if (this.subject instanceof ExecutorAware) {
            final ExecutorAware ea = (ExecutorAware) this.subject;
            final ExecutorService executor = Mockito.mock(ExecutorService.class);
            ea.setExecutorService(executor);
            final StringListener listener = Mockito.mock(StringListener.class);
            this.subject.listeners().add(StringListener.class, listener);

            this.subject.dispatch(new StringEvent(this.subject, ""),
                    StringListener::onStringEvent);

            Mockito.verify(executor).execute(Mockito.any());
        }
    }
}