package de.skuzzle.jeve;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mockito;

import de.skuzzle.jeve.stores.DefaultListenerStore;
import de.skuzzle.jeve.util.AbstractEventProviderTest;
import de.skuzzle.jeve.util.StringEvent;
import de.skuzzle.jeve.util.StringListener;

/**
 * Tests asynchronous EventProviders for whether they correctly handle
 * exceptions thrown by the
 * {@link java.util.concurrent.ExecutorService#execute(Runnable)} method.
 *
 * @author Simon Taddiken
 * @since 1.1.0
 */
@RunWith(Parameterized.class)
public class BrokenExecutorServiceEventProviderTest extends
        AbstractEventProviderTest<DefaultListenerStore> {

    /**
     * ExecutorService which always throws an Exception when trying to execute a
     * Runnable
     *
     * @author Simon Taddiken
     */
    private static final class BrokenExecutorService extends ThreadPoolExecutor {

        public BrokenExecutorService() {
            super(1, 1, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));
        }

        @Override
        public void execute(Runnable command) {
            throw new RejectedExecutionException();
        }
    }

    /**
     * Parameterizes the test instances.
     *
     * @return Collection of parameters for the constructor of
     *         {@link EventProviderTestBase}.
     */
    @Parameters
    public static final Collection<Object[]> getParameters() {
        return Collections.singleton(
                new Object[] {
                        EventProvider.configure()
                                .defaultStore()
                                .useParallelProvider().and()
                                .executor(BrokenExecutorService::new)
                                .createSupplier()
                });
    }

    /**
     * Creates a new Test class instance.
     *
     * @param factory A factory for creating event providers
     */
    public BrokenExecutorServiceEventProviderTest(
            Supplier<? extends EventProvider<DefaultListenerStore>> factory) {
        super(factory);
    }

    /**
     * Tests whether the subject correctly handles exceptions thrown if the
     * executor is not ready for dispatching.
     *
     * @throws Exception If the test case fails for any reason.
     */
    @Test
    public void testHandleException() throws Exception {
        final ExceptionCallback ec = Mockito.mock(ExceptionCallback.class);
        this.subject.setExceptionCallback(ec);
        this.subject.listeners().add(StringListener.class, se -> {
        });
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        sleep(); // HACK: give async providers some time to execute
        Mockito.verify(ec).exception(Mockito.any(), Mockito.any(), Mockito.any());
    }
}
