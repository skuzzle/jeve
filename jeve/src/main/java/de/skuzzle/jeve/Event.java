package de.skuzzle.jeve;


/**
 * <p>
 * This class is the base of all events that can be fired. It holds the source
 * of the event and provides methods to stop delegation to further listeners if
 * this event has been handled by one listener. Events are meant to be short
 * living objects which are only used once - one Event instance for each call to
 * {@link EventProvider#dispatch(Event, java.util.function.BiConsumer) dispatch}
 * . Any different usage might result in undefined behavior, especially when
 * using the {@link #isHandled()} property. Events explicitly belong to one kind
 * of {@link Listener} implementation which is able to handle it. The class of
 * this listener is passed to the constructor and queried by the
 * {@link EventProvider} when collecting a list of targeted listeners for a
 * dispatch action.
 * </p>
 *
 * <p>
 * Events are used in conjunction with the {@link EventProvider} and its
 * {@link EventProvider#dispatch(Event, java.util.function.BiConsumer, ExceptionCallback)
 * dispatch} method. The dispatch method serves for notifying all registered
 * listeners with a certain event. The EventProvider will stop notifying further
 * listeners as soon as one listener sets this class' {@link #isHandled()} to
 * <code>true</code>.
 * </p>
 *
 * @author Simon Taddiken
 * @since 1.0.0
 * @param <T> Type of the source of this event.
 * @param <L> Type of the listener which can handle this event.
 */
public class Event<T, L extends Listener> {

    /** The source of the event */
    private final T source;

    /** Whether this event has been marked as handled */
    private boolean isHandled;

    /** The class of the listener which can handle this event */
    private final Class<L> listenerClass;

    /**
     * Creates a new event with a given source.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event.
     */
    public Event(T source, Class<L> listenerClass) {
        this.source = source;
        this.listenerClass = listenerClass;
        this.isHandled = false;
    }



    /**
     * Gets the source of this event.
     *
     * @return The source of this event.
     */
    public T getSource() {
        return this.source;
    }



    /**
     * Gets the type of the listener which can handle this event.
     *
     * @return The listener's type.
     * @version 2.0.0
     */
    protected Class<L> getListenerClass() {
        return this.listenerClass;
    }


    /**
     * Gets whether this was already handled. If this returns <code>true</code>, no
     * further listeners will be notified about this event.
     *
     * @return Whether this event was handled.
     */
    public boolean isHandled() {
        return this.isHandled;
    }



    /**
     * Sets whether this event was already handled. If an event has been marked as
     * "handled", no further listeners will be notified about it.
     *
     * <p>Note that setting an event to be handled might have unintended side effects
     * when using an {@link EventProvider} which is not
     * {@link EventProvider#isSequential() sequential}.</p>
     *
     * @param isHandled Whether this event was handled.
     */
    public void setHandled(boolean isHandled) {
        this.isHandled = isHandled;
    }
}