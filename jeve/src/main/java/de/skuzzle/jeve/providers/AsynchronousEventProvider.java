package de.skuzzle.jeve.providers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

/**
 * This EventProvider fires events asynchronously using an
 * {@link ExecutorService} for managing the creation of threads.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
public class AsynchronousEventProvider extends AbstractEventProvider
        implements ExecutorAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProvider.class);

    private static final long TERMINATION_TIMEOUT = 2000;

    /** Service which serves for creating and reusing threads. */
    protected ExecutorService executor;

    private boolean blocking;

    /**
     * Creates a new {@link AsynchronousEventProvider} which uses a single
     * threaded {@link ExecutorService}.
     *
     * @param source Responsible for storing and retrieving listeners of this
     *            provider.
     */
    public AsynchronousEventProvider(ListenerSource source) {
        this(source, Executors.newFixedThreadPool(1));
    }

    /**
     * Creates a new {@link AsynchronousEventProvider} which uses the provided
     * {@link ExecutorService} for event dispatching.
     *
     * @param source Responsible for storing and retrieving listeners of this
     *            provider.
     * @param executor ExecutorService to use.
     */
    public AsynchronousEventProvider(ListenerSource source, ExecutorService executor) {
        super(source);
        if (executor == null) {
            throw new IllegalArgumentException("executor is null");
        }
        this.executor = executor;
    }

    @Override
    public void setExecutorService(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor is null");
        }

        this.executor = executor;
    }

    protected ExecutorService getExecutor() {
        return this.executor;
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {

        checkDispatchArgs(event, bc, ec);
        if (canDispatch()) {
            final Future<?> future = this.executor.submit(
                    () -> notifyListeners(event, bc, ec));

            waitIfNecessary(future);
        }
    }

    private void waitIfNecessary(Future<?> future) {
        if (this.blocking) {
            try {
                future.get();
            } catch (final InterruptedException e) {
                LOGGER.error("Error while waiting for all listeners to be notified.", e);
                Thread.currentThread().interrupt();
            } catch (final ExecutionException e) {
                LOGGER.error("Error while waiting for all listeners to be notified.", e);
            }
        }
    }

    @Override
    public boolean canDispatch() {
        return !this.executor.isShutdown() && !this.executor.isTerminated();
    }

    @Override
    public void close() {
        super.close();
        this.executor.shutdownNow();
        try {
            this.executor.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            LOGGER.error("AsynchronousEventProvider: Error while waiting for "
                    + "termination of executor", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected boolean isImplementationSequential() {
        return true;
    }
}
