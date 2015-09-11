package de.skuzzle.jeve.providers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

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
 * @author Simon Taddiken
 * @since 1.1.0
 */
public class ParallelEventProvider extends AbstractEventProvider
        implements ExecutorAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProvider.class);
    private static final long TERMINATION_TIMEOUT = 2000;

    private ExecutorService executor;

    /**
     * Creates a new ParallelEventProvider using the provided store.
     *
     * @param source Responsible for storing and retrieving listeners of this
     *            provider.
     */
    public ParallelEventProvider(ListenerSource source) {
        this(source, Executors.newCachedThreadPool());
    }

    /**
     * Creates a new ParallelEventPRovider.
     *
     * @param source Responsible for storing and retrieving listeners of this
     *            provider.
     * @param executor The executor to use.
     */
    public ParallelEventProvider(ListenerSource source, ExecutorService executor) {
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

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {

        checkDispatchArgs(event, bc, ec);
        if (!canDispatch()) {
            return;
        }

        final Stream<L> listeners = getSource().get(event.getListenerClass());
        listeners.forEach(listener -> {
                this.executor.execute(() -> notifySingle(listener, event, bc, ec));
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
        } catch (final InterruptedException e) {
            LOGGER.error("ParallelEventProvider: Error while waiting for termination "
                    + "of executor", e);
        }
    }

    @Override
    protected boolean isImplementationSequential() {
        return false;
    }
}
