package de.skuzzle.jeve;

import java.util.Optional;

/**
 *
 * @param <T> Type of the source of this event.
 * @param <L> Type of the listener which can handle this event.
 * @author Simon Taddiken
 * @since 3.0.0
 */
public abstract class DefaultDispatchEvent<T, L extends Listener> extends Event<T, L> {

    public DefaultDispatchEvent(T source, Class<L> listenerClass, Event<?, ?> cause) {
        super(source, listenerClass, cause);
    }

    public DefaultDispatchEvent(T source, Class<L> listenerClass,
            Optional<Event<?, ?>> cause) {
        super(source, listenerClass, cause);
    }

    public DefaultDispatchEvent(T source, Class<L> listenerClass) {
        super(source, listenerClass);
    }

    /**
     * <p>
     * Dispatches this event with the given EventProvider using the listener's
     * default listening method. For example, if {@code userAdded} is the only
     * listening method (or the default one among others), this method should be
     * implemented as follows:
     * </p>
     *
     * <pre>
     * public void defaultDispatch(EventProvider&lt;?&gt; eventProvider, ExceptionCallback ec) {
     *     eventProvider.dispatch(this, UserListener::userAdded, ec);
     * }
     * </pre>
     *
     * <p>
     * This method should not be called directly on an Event object. Instead,
     * pass the event to {@link EventProvider#dispatch(Event)} or
     * {@link EventProvider#dispatch(Event, ExceptionCallback)}.
     * </p>
     *
     * <p>
     * The default implementation throws an
     * {@link UnsupportedOperationException}
     * </p>
     *
     * @param eventProvider The EventProvider to use for dispatching.
     * @param ec The exception call back to use for this dispatch action.
     * @since 3.0.0
     */
    public abstract void defaultDispatch(EventProvider<?> eventProvider,
            ExceptionCallback ec);
}
