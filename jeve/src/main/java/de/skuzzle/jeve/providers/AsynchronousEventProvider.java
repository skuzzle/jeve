package de.skuzzle.jeve.providers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * This EventProvider fires events asynchronously using an
 * {@link ExecutorService} for managing the creation of threads.
 *
 * @param <S> The type of the ListenerStore this provider uses.
 * @author Simon Taddiken
 * @since 1.0.0
 */
public class AsynchronousEventProvider<S extends ListenerStore> extends
        AbstractEventProvider<S> implements ExecutorAware {

    /** Service which serves for creating and reusing threads. */
    protected ExecutorService executor;

    /**
     * Creates a new {@link AsynchronousEventProvider} which uses a single
     * threaded {@link ExecutorService}.
     *
     * @param store Responsible for storing and retrieving listeners of this
     *            provider.
     */
    public AsynchronousEventProvider(S store) {
        this(store, Executors.newFixedThreadPool(1));
    }

    /**
     * Creates a new {@link AsynchronousEventProvider} which uses the provided
     * {@link ExecutorService} for event dispatching.
     *
     * @param store Responsible for storing and retrieving listeners of this
     *            provider.
     * @param executor ExecutorService to use.
     */
    public AsynchronousEventProvider(S store, ExecutorService executor) {
        super(store);
        if (executor == null) {
            throw new IllegalArgumentException("dispatcher is null");
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

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {

        checkDispatchArgs(event, bc, ec);
        if (canDispatch()) {
            this.executor.execute(() -> notifyListeners(event, bc, ec));
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
            this.executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean isImplementationSequential() {
        return true;
    }
}
