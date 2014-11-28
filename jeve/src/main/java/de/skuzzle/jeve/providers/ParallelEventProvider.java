package de.skuzzle.jeve.providers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * EventProvider implementation which uses an {@link ExecutorService} to notify
 * each listener within a dedicated thread. This implementation is thereby
 * {@link #isSequential() not sequential}.
 *
 * <p>
 * Instances of this class can be obtained using the static factory methods of
 * the {@link EventProvider} interface.
 * </p>
 *
 * @param <S> The type of the ListenerStore this provider uses.
 * @author Simon Taddiken
 * @since 1.1.0
 */
public class ParallelEventProvider<S extends ListenerStore> extends
        AbstractEventProvider<S> implements ExecutorAware {

    private static final long TERMINATION_TIMEOUT = 2000;

    private ExecutorService executor;

    /**
     * Creates a new ParallelEventProvider using the provided store.
     *
     * @param store Responsible for storing and retrieving listeners of this
     *            provider.
     */
    public ParallelEventProvider(S store) {
        this(store, Executors.newCachedThreadPool());
    }

    /**
     * Creates a new ParallelEventPRovider.
     *
     * @param store Responsible for storing and retrieving listeners of this
     *            provider.
     * @param executor The executor to use.
     */
    public ParallelEventProvider(S store, ExecutorService executor) {
        super(store);
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

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {

        checkDispatchArgs(event, bc, ec);
        if (!canDispatch()) {
            return;
        }

        final Stream<L> listeners = listeners().get(event.getListenerClass());
        event.setListenerStore(listeners());
        listeners.forEach(listener -> {
            try {
                this.executor.execute(() -> notifySingle(listener, event, bc, ec));
            } catch (RuntimeException e) {
                handleException(ec, e, listener, event);
            }
        });
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
        } catch (InterruptedException e) {
            this.logger.error("Error while waiting for termination of executor", e);
        }
    }

    @Override
    protected boolean isImplementationSequential() {
        return false;
    }
}
