package de.skuzzle.jeve;

import java.util.function.BiConsumer;

/**
 * Provides a convenient way to implement Listeners which only have a single
 * listening method. If a listener only has a single listening method, it is
 * redundant to specify it when calling the {@link EventProvider
 * EventProvider's} {@code dispatch} method. As dispatch may be called often for
 * some kind of events, it is easier to statically provide the method reference
 * to the listening method. That is what this event class is for.
 *
 * <p>
 * Due to Java's type system, this kind of events need a third type parameter
 * {@code SELF} which must be set to the class you are implementing. Here is a
 * sample implementation:
 * </p>
 *
 * <pre>
 * <code>
 * public class UserEvent extends DefaultTargetEvent&lt;UserManager, UserEvent, UserListener&gt; {
 *
 *     public UserEvent(UserManager source) {
 *         super(source, UserListener.class);
 *     }
 *
 *     &#64;Override
 *     public BiConsumer&lt;UserListener, UserEvent&gt; getTarget() {
 *         return UserListener::userAdded;
 *     }
 * }
 * </code>
 * </pre>
 * <p>
 * As you can see, the middle parameter is set to the implementing class's name
 * itself to be available as type parameter for the return type of
 * {@link #getTarget()}. Thereby, these events are completely compatible with
 * the EventProvider's {@code dispatch} method:
 * </p>
 *
 * <pre>
 * <code>eventProvider.dispatch(event, event.getTarget())</code>
 * </pre>
 *
 * <p>
 * This allows for the creation of a more convenient dispatch overload which
 * only takes the event as parameter and internally delegates to the above shown
 * overload:
 * </p>
 *
 * <pre>
 * <code>eventProvider.dispatch(event)</code>
 * </pre>
 *
 * @author Simon Taddiken
 * @param <T> Type of the source of this event.
 * @param <SELF> The concrete type of the extending class.
 * @param <L> Type of the listener which can handle this event.
 * @see Event
 * @since 2.0.0
 */
public abstract class DefaultTargetEvent<T, SELF extends Event<?, L>, L extends Listener>
        extends Event<T, L> {

    /**
     * Creates a new event with a given source.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     */
    public DefaultTargetEvent(T source, Class<L> listenerClass) {
        super(source, listenerClass);
    }

    /**
     * <p>
     * Dispatches this event with the given EventProvider using the listener's
     * default listening method. For example, if {@code userAdded} is the only
     * listeneing method (or the default one among others), this method should
     * be implemented as follows:
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
     * pass the event to {@link EventProvider#dispatch(DefaultTargetEvent)} or
     * {@link EventProvider#dispatch(DefaultTargetEvent, ExceptionCallback)}.
     * </p>
     *
     * @param eventProvider The EventProvider to use for dispatching.
     * @param ec The exception call back to use for this dispatch action.
     * @since 2.1.0
     */
    public void dispatch(EventProvider<?> eventProvider, ExceptionCallback ec) {
        if (eventProvider == null) {
            throw new IllegalArgumentException("eventProvider is null");
        }
        eventProvider.dispatch((SELF) this, getTarget(), ec);
    }

    /**
     * Returns a method reference to the method of the listener which should be
     * called with this event.
     *
     * @return A method reference to a listening method of a listener.
     * @deprecated Since 2.1.0 - override
     *             {@link #dispatch(EventProvider, ExceptionCallback)} instead.
     */
    @Deprecated
    public abstract BiConsumer<L, SELF> getTarget();
}
