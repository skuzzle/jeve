package de.skuzzle.jeve;

import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Provides utility methods for working with the {@link EventStack}.
 *
 * @author Simon Taddiken
 * @since 2.1.0
 */
public final class EventStackHelper {

    private EventStackHelper() {
        // hidden constructor
    }

    /**
     * Checks whether the given Event should be prevented according to the given
     * event stack. If so, the event's {@link Event#isPrevented() isPrevented}
     * property is set to <code>true</code> and a new {@link SuppressedEvent} is
     * added to the <em>preventing</em> event.
     *
     * @param <L> Type of the listener.
     * @param <E> Type of the event.
     * @param eventStack The event stack.
     * @param event The Event to check whether it is prevented.
     * @param bc Function to delegate the event to the specific callback method
     *            of the listener.
     * @param ec Callback to be notified when any of the listeners throws an
     *            exception.
     * @return Whether the event should be prevented.
     */
    public static <L extends Listener, E extends Event<?, L>> boolean checkPrevent(
            EventStack eventStack, E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        // check if any of the currently dispatched events marked the target
        // listener class to be prevented.
        final Optional<SynchronousEvent<?, ?>> cause = eventStack.preventDispatch(
                event.getListenerClass());
        if (cause.isPresent()) {
            event.setPrevented(true);
            cause.get().addSuppressedEvent(
                    new SuppressedEventImpl<L, E>(event, ec, bc));
            return true;
        }
        return false;
    }

    static class SuppressedEventImpl<L extends Listener, E extends Event<?, L>>
            implements SuppressedEvent {

        private final E event;
        private final ExceptionCallback ec;
        private final BiConsumer<L, E> consumer;
        private boolean dispatched;

        public SuppressedEventImpl(E event, ExceptionCallback ec,
                BiConsumer<L, E> consumer) {
            this.event = event;
            this.ec = ec;
            this.consumer = consumer;
        }

        @Override
        public Class<L> getListenerClass() {
            return this.event.getListenerClass();
        }

        @Override
        public E getEvent() {
            return this.event;
        }

        @Override
        public void redispatch(EventProvider<?> provider) {
            if (!this.dispatched) {
                this.dispatched = true;
                provider.dispatch(this.event, this.consumer, this.ec);
            }
        }

        @Override
        public int hashCode() {
            return this.event.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof SuppressedEvent &&
                    this.event.equals(((SuppressedEvent) obj).getEvent());
        }
    }
}
