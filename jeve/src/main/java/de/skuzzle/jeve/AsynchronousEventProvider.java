package de.skuzzle.jeve;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * This EventProvider fires events asynchronously using an
 * {@link ExecutorService} for managing the creation of threads.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
class AsynchronousEventProvider extends AbstractEventProvider {

    /** Service which serves for creating and reusing threads. */
    protected final ExecutorService dispatchPool;

    /**
     * Creates a new {@link AsynchronousEventProvider} which uses a single
     * threaded {@link ExecutorService}.
     */
    public AsynchronousEventProvider() {
        this(Executors.newFixedThreadPool(1));
    }

    /**
     * Creates a new {@link AsynchronousEventProvider} which uses the provided
     * {@link ExecutorService} for event dispatching.
     *
     * @param dispatcher ExecutorService to use.
     */
    public AsynchronousEventProvider(ExecutorService dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("dispatcher is null");
        }
        this.dispatchPool = dispatcher;
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {

        checkDispatchArgs(event, bc, ec);
        if (canDispatch()) {
            this.dispatchPool.execute(() -> notifyListeners(event, bc, ec));
        }

    }

    @Override
    public boolean canDispatch() {
        return !this.dispatchPool.isShutdown() && !this.dispatchPool.isTerminated();
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    @Override
    public void close() {
        super.close();
        this.dispatchPool.shutdownNow();
        try {
            this.dispatchPool.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
