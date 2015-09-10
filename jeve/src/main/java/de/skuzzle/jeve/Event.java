package de.skuzzle.jeve;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * This class is the base of all events that can be fired. It holds the source
 * of the event and provides methods to stop delegation to further listeners if
 * this event has been handled by one listener. Events are meant to be short
 * living objects which are only used once - one Event instance for each call to
 * {@link EventProvider#dispatch(Event, java.util.function.BiConsumer) dispatch}
 * . Any different usage might result in undefined behavior, especially when
 * using the {@link #isHandled()} property. Also, to prevent memory leaks, Event
 * objects should never be stored over longer time.
 * </p>
 * <p>
 * Events explicitly belong to one kind of {@link Listener} implementation which
 * is able to handle it. The class of this listener is passed to the constructor
 * and queried by the {@link EventProvider} when collecting a list of targeted
 * listeners for a dispatch action.
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
 * <h2>Note on multi-threading</h2>
 * <p>
 * Event objects are not thread safe! Some EventProviders dispatch events
 * asynchronously. If the same event instance is used within different threads,
 * avoid modifying properties of the Event. Those modifications will result in
 * undefined behavior.
 * </p>
 *
 * @param <T> Type of the source of this event.
 * @param <L> Type of the listener which can handle this event.
 * @author Simon Taddiken
 * @since 1.0.0
 * @version 2.0.0
 */
public class Event<T, L extends Listener> {

    /** The source of the event */
    private final T source;

    /** Whether this event has been marked as handled */
    private boolean handled;

    /** The class of the listener which can handle this event */
    private final Class<L> listenerClass;

    /**
     * Whether this event was prevented the last time it was passed to any
     * dispatch method.
     *
     * @since 3.0.0
     */
    private boolean prevented;

    /**
     * Map for assigning further objects to this event. Map is lazily
     * initialized.
     */
    private Map<String, Object> properties;

    /**
     * Creates a new event with a given source.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     */
    public Event(T source, Class<L> listenerClass) {
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        } else if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        }

        this.source = source;
        this.listenerClass = listenerClass;
        this.handled = false;
    }

    /**
     * Returns the Map that is used to store properties in this event instance.
     * All modifications to that map are directly reflected to its Event
     * instance.
     *
     * @return The property Map of this event.
     * @since 3.0.0
     */
    public Map<String, Object> getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        return this.properties;
    }

    /**
     * Returns a value that has been stored using
     * {@link #setValue(String, Object)}. As this method is generic, the value
     * will automatically be casted to the target type of the expression in
     * which this method is called. So this method has to be used with caution
     * in respect to type safety.
     *
     * @param key The key of the value to retrieve.
     * @return The value or an empty optional if the value does not exist.
     * @since 3.0.0
     */
    @SuppressWarnings("unchecked")
    public <E> Optional<E> getValue(String key) {
        if (this.properties == null) {
            return Optional.empty();
        }
        return Optional.ofNullable((E) getProperties().get(key));
    }

    /**
     * Adds the given key-value mapping to this Event. The value can be queried
     * using {@link #getValue(String)}.
     * <p>
     * <b>Note:</b> Storing properties on events is generally discouraged in
     * favor of creating explicit attributes with getters. However, there might
     * arise situations in which you must attach further properties to an event
     * without being able to modify its original source code. In these
     * situations, storing properties with a String key might be useful.
     *
     * @param key The key under which the value is stored.
     * @param value The value to store.
     * @since 3.0.0
     * @see #getValue(String)
     * @see #getProperties()
     */
    public void setValue(String key, Object value) {
        getProperties().put(key, value);
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
     * Whether this event was prevented the last time it has been passed to any
     * dispatch method of an {@link EventProvider}.
     *
     * @return Whether this event has been prevented.
     */
    public boolean isPrevented() {
        return this.prevented;
    }

    /**
     * Called only by {@link EventProvider EventProvider's} dispatch method if
     * this event was prevented from being dispatched.
     *
     * @param prevented Whether the event was prevented.
     */
    public void setPrevented(boolean prevented) {
        this.prevented = prevented;
    }

    /**
     * Gets the type of the listener which can handle this event.
     *
     * @return The listener's type.
     * @version 2.0.0
     */
    public Class<L> getListenerClass() {
        return this.listenerClass;
    }

    /**
     * Gets whether this event was already handled. If this returns
     * <code>true</code>, no further listeners will be notified about this
     * event.
     *
     * @return Whether this event was handled.
     */
    public boolean isHandled() {
        return this.handled;
    }

    /**
     * Sets whether this event was already handled. If an event has been marked
     * as "handled", no further listeners will be notified about it.
     *
     * <p>
     * Note that setting an event to be handled might have unintended side
     * effects when using an {@link EventProvider} which is not
     * {@link EventProvider#isSequential() sequential}.
     * </p>
     *
     * @param isHandled Whether this event was handled.
     */
    public void setHandled(boolean isHandled) {
        this.handled = isHandled;
    }
}
