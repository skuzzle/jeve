package de.skuzzle.jeve;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.skuzzle.jeve.util.AbstractEventProviderTest;
import de.skuzzle.jeve.util.EventProviderFactory;
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
public class BrokenExecutorServiceEventProviderTest extends AbstractEventProviderTest {

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
                new EventProviderFactory[] {
                        () -> EventProviders.newParallelEventProvider(
                                new BrokenExecutorService()) }
                );
    }

    /**
     * Creates a new Test class instance.
     *
     * @param factory A factory for creating event providers
     */
    public BrokenExecutorServiceEventProviderTest(EventProviderFactory factory) {
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
        final boolean[] exception = new boolean[1];
        final ExceptionCallback ec = new ExceptionCallback() {
            @Override
            public void exception(Exception e, Listener source, Event<?, ?> event) {
                exception[0] = true;
            }
        };
        this.subject.setExceptionCallback(ec);
        this.subject.addListener(StringListener.class, se -> {
        });
        final StringEvent e = new StringEvent(this.subject, "");
        this.subject.dispatch(e, StringListener::onStringEvent);

        sleep(); // HACK: give async providers some time to execute
        Assert.assertTrue(getFailString("Exception handler not called"), exception[0]);
    }
}
