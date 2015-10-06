package de.skuzzle.jeve;

import java.util.stream.Stream;

/**
 * Supplies listeners to an {@link EventProvider}.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public interface ListenerSource {

    /**
     * <p>
     * Returns a thread safe view of this source. The returned source will be
     * backed by this one, thus changes on the one object are automatically
     * reflected to the other. This also implies that unsynchronized access is
     * still possible through the original object.
     * </p>
     * <p>
     * Subsequent calls to this method must return a cached instance of the
     * synchronized view. Source implementations that are natively thread safe
     * may simply return {@code this}.
     * </p>
     *
     * <p>
     * <b>Note:</b> The actual returned object must be assignment compatible
     * with this store, thus for the implementation 'MyListenerStore' the
     * following statement must compile:
     * </p>
     *
     * <pre>
     * MyListenerSource source = new MyListenerSource().synchronizedView();
     * </pre>
     *
     * @return A thread safe view of this source.
     * @since 3.0.0
     */
    public ListenerSource synchronizedView();

    /**
     * Gets all listeners that should be notified for the given listenerClass.
     * The returned Stream must NOT be backed by the source's internal
     * collection which holds the listeners unless this source's collection of
     * listeners is unmodifiable.
     *
     * @param <L> Type of the listeners to return.
     * @param listenerClass The class representing the event for which the
     *            listeners should be retrieved.
     * @return A Stream of listeners that should be notified about the event
     *         represented by the given listener class.
     * @throws IllegalArgumentException If listenerClass is <code>null</code>.
     */
    <L extends Listener> Stream<L> get(Class<L> listenerClass);

    /**
     * Gets all listeners that should be notified for the given event. This is
     * short hand for {@code ListenerSource.get(event.getListenerClass)}.
     *
     * @param <L> The listener type.
     * @param event The event to get the listeners to be notified for.
     * @return A Stream of listeners that should be notified about the event
     *         represented by the given listener class.
     * @throws IllegalArgumentException If the event is <code>null</code>.
     */
    default <L extends Listener> Stream<L> get(Event<?, L> event) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }
        return get(event.getListenerClass());
    }

    /**
     * States whether this ListenerSource implementation is sequential. This is
     * the case if, and only if the Stream returned by {@link #get(Class)}
     * returns the registered Listeners in FIFO order regarding their time of
     * registration.
     *
     * @return Whether this store is sequential.
     */
    public boolean isSequential();

    /**
     * Releases resources held by this source.
     */
    public void close();
}