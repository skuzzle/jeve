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
 * {@code E} which must be set to the class you are implementing. Here is a
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
 * <code>eventProvider.dispatch(event, event.getTarget())</code>
 * </pre>
 *
 * @author Simon Taddiken
 * @param <T> Type of the source of this event.
 * @param <E> The concrete type of the extending class.
 * @param <L> Type of the listener which can handle this event.
 * @see Event
 * @since 2.0.0
 */
public abstract class DefaultTargetEvent<T, E extends DefaultTargetEvent<T, ?, L>, L extends Listener>
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
     * Returns a method reference to the method of the listener which should be
     * called with this event.
     *
     * @return A method reference to a listening method of a listener.
     */
    public abstract BiConsumer<L, E> getTarget();
}
