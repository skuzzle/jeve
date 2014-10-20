package de.skuzzle.jeve;

/**
 *
 * @author Simon Taddiken
 *
 * @param <T> Type of the source of this event.
 * @param <L> Type of the listener which can handle this event.
 * @since 2.1.0
 */
public abstract class DoubleDispatchedEvent<T, L extends Listener> extends Event<T, L> {

    /**
     * Creates a new event with a given source.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     */
    public DoubleDispatchedEvent(T source, Class<L> listenerClass) {
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
     * public void dispatch(EventProvider&lt;?&gt; eventProvider, ExceptionCallback ec) {
     *     eventProvider.dispatch(this, UserListener::userAdded, ec);
     * }
     * </pre>
     *
     * <p>
     * This method should not be called directly on an Event object. Instead,
     * pass the event to {@link EventProvider#dispatch(DoubleDispatchedEvent)}
     * or
     * {@link EventProvider#dispatch(DoubleDispatchedEvent, ExceptionCallback)}.
     * </p>
     *
     * @param eventProvider The EventProvider to use for dispatching.
     * @param ec The exception call back to use for this dispatch action.
     */
    public abstract void dispatch(EventProvider<?> eventProvider, ExceptionCallback ec);
}
