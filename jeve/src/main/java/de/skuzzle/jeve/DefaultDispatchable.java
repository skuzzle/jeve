package de.skuzzle.jeve;


/**
 * This interface is ought to be implemented by Event subclasses to provide a
 * simple default dispatch mechanism. Objects implementing this interface can be
 * passed to {@link EventProvider#dispatch(DefaultDispatchable)}.
 *
 * @author Simon Taddiken
 * @since 3.0.0
 */
public interface DefaultDispatchable {

    /**
     * <p>
     * Dispatches this event with the given EventProvider using the listener's
     * default listening method. For example, if {@code userAdded} is the only
     * listening method (or the default one among others), this method should be
     * implemented as follows:
     * </p>
     *
     * <pre>
     * &#064;Override
     * public void defaultDispatch(EventProvider&lt;?&gt; provider, ExceptionCallback ec) {
     *     eventProvider.dispatch(this, UserListener::userAdded, ec);
     * }
     * </pre>
     *
     * <p>
     * This method should not be called directly on an Event object. Instead,
     * pass the event to {@link EventProvider#dispatch(DefaultDispatchable)} or
     * {@link EventProvider#dispatch(DefaultDispatchable, ExceptionCallback)}.
     * </p>
     *
     * @param provider The EventProvider to use for dispatching.
     * @param ec The exception call back to use for this dispatch action.
     */
    public void defaultDispatch(EventProvider provider, ExceptionCallback ec);
}
