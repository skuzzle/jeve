package de.skuzzle.jeve.providers;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * Provider which queues events while dispatching. Thus nested dispatch calls are handled
 * deferred after the previous dispatch action finished.
 *
 * @author Simon Taddiken
 * @param <S> The type of the ListenerStore this provider uses.
 * @since 3.0.0
 */
public class UnrollingEventProvider<S extends ListenerStore> extends
        AbstractEventProvider<S> {

    private class QueuedEvent<L extends Listener, E extends Event<?, L>> {

        private final E event;
        private final ExceptionCallback ec;
        private final BiConsumer<L, E> consumer;

        private QueuedEvent(E event, ExceptionCallback ec, BiConsumer<L, E> consumer) {
            super();
            this.event = event;
            this.ec = ec;
            this.consumer = consumer;
        }

        private void dispatch() {
            UnrollingEventProvider.super.notifyListeners(this.event,
                    this.consumer, this.ec);
        }
    }

    private final Queue<QueuedEvent<?, ?>> dispatchQueue;

    private boolean dispatchInProgress;

    /**
     * Creates a new UnrollingEventProvider using the given ListenerStore.
     *
     * @param store the store which supplies listeners to this provider.
     */
    public UnrollingEventProvider(S store) {
        super(store);
        this.dispatchQueue = new ArrayDeque<>();
    }

    @Override
    protected <L extends Listener, E extends Event<?, L>> void notifyListeners(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {

        if (this.dispatchInProgress) {
            this.dispatchQueue.add(new QueuedEvent<L, E>(event, ec, bc));
            return;
        }

        try {
            this.dispatchInProgress = true;

            super.notifyListeners(event, bc, ec);

            while (!this.dispatchQueue.isEmpty()) {
                final QueuedEvent<?, ?> next = this.dispatchQueue.poll();
                next.dispatch();
            }
        } finally {
            this.dispatchInProgress = false;
        }
    }

    @Override
    public boolean canDispatch() {
        return true;
    }

    @Override
    protected boolean isImplementationSequential() {
        return true;
    }

}
