package de.skuzzle.jeve.providers;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.EventStack;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.SuppressedEvent;

/**
 * {@link EventProvider} implementation which is always ready for dispatching
 * and simply runs all listeners within the current thread.
 *
 * @param <S> The type of the ListenerStore this provider uses.
 * @author Simon Taddiken
 * @since 1.0.0
 */
public class SynchronousEventProvider<S extends ListenerStore> extends
        AbstractEventProvider<S> {

    private final EventStack eventStack;

    /**
     * Creates a new SynchronousEventProvider.
     *
     * @param store Responsible for storing and retrieving listeners of this
     *            provider.
     */
    public SynchronousEventProvider(S store) {
        super(store);
        this.eventStack = new EventStack();
    }

    /**
     * Checks whether the given Event should be prevented according to the
     * current event stack. If so, the event's {@link Event#isPrevented()
     * isPrevented} property is set to <code>true</code> and a new
     * {@link SuppressedEvent} is added to the <em>preventing</em> event.
     *
     * @param event The Event to check whether it is prevented.
     * @param bc Function to delegate the event to the specific callback method
     *            of the listener.
     * @param ec Callback to be notified when any of the listeners throws an
     *            exception.
     * @return Whether the event should be prevented.
     */
    protected <L extends Listener, E extends Event<?, L>> boolean checkPrevent(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        // check if any of the currently dispatched events marked the target
        // listener class to be prevented.
        final Optional<Event<?, ?>> preCascade = this.eventStack.preventDispatch(
                event.getListenerClass());
        if (preCascade.isPresent()) {
            this.logger.debug("Dispatch prevented for '{}' by '{}'",
                    event, preCascade.get());
            event.setPrevented(true);
            preCascade.get().addSuppressedEvent(
                    new SuppressedEventImpl<L, E>(event, ec, bc));
            return true;
        }
        return false;
    }

    @Override
    protected <L extends Listener, E extends Event<?, L>> boolean notifyListeners(
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        if (checkPrevent(event, bc, ec)) {
            return false;
        }

        // HINT: getListeners is thread safe
        final Stream<L> listeners = listeners().get(event.getListenerClass());
        boolean result = true;

        try {
            event.setListenerStore(listeners());
            this.eventStack.pushEvent(event);
            final Iterator<L> it = listeners.iterator();
            while (it.hasNext()) {
                final L listener = it.next();
                if (event.isHandled()) {
                    return result;
                }
                result &= notifySingle(listener, event, bc, ec);
            }
        } finally {
            this.eventStack.popEvent(event);
        }
        return result;
    }

    /**
     * Gets the current event stack. It holds information about nested
     * dispatches in progress.
     *
     * @return The event stack.
     */
    public EventStack getEventStack() {
        return this.eventStack;
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
