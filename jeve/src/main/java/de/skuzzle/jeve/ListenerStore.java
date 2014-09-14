package de.skuzzle.jeve;

import java.io.Closeable;
import java.util.stream.Stream;

public interface ListenerStore extends Closeable {

    /**
     * Adds a listener which will be notified for every event represented by the
     * given listener class. After registration, the listener's
     * {@link Listener#onRegister(RegistrationEvent) onRegister} method gets
     * called to notify the listener about being added to a new parent. The
     * {@code onRegister} method is not subject to the dispatching strategy
     * implemented by this {@link EventProvider} and is called from the current
     * thread.
     *
     * <p>
     * <b>Note on concurrency:</b> This method can safely be called from within
     * a listening method during event handling to add a listener. This will
     * have no impact on the current event delegation process.
     * </p>
     *
     * @param <L> Type of the listener to add.
     * @param listenerClass The class representing the event(s) to listen on.
     * @param listener The listener to add.
     * @throws IllegalArgumentException If either listenerClass or listener
     *             argument is <code>null</code>.
     */
    public <L extends Listener> void add(Class<L> listenerClass, L listener);

    /**
     * Removes a listener. It will only be removed for the specified listener
     * class and can thus still be registered with this event provider if it was
     * added for further listener classes. The listener will no longer receive
     * events represented by the given listener class. After removal, the
     * listener's {@link Listener#onUnregister(RegistrationEvent) onUnregister}
     * method gets called to notify the listener about being removed from a
     * parent. The {@code onUnregister} method is not subject to the dispatching
     * strategy implemented by this {@link EventProvider} and is called from the
     * current thread.
     *
     * <p>
     * If any of the arguments is <code>null</code>, this method returns with no
     * effect.
     * </p>
     *
     * <p>
     * <b>Note on concurrency:</b> This method can safely be called from within
     * a listening method during event handling to remove a listener. This will
     * have no impact on the current event delegation process.
     * </p>
     *
     * @param <L> Type of the listener to remove.
     * @param listenerClass The class representing the event(s) for which the
     *            listener should be removed.
     * @param listener The listener to remove.
     */
    public <L extends Listener> void remove(Class<L> listenerClass, L listener);

    /**
     * Gets all listeners that have been registered using
     * {@link #add(Class, Listener)} for the given listener class. The returned
     * collection contains the listeners in the order in which they have been
     * registered. Modifying the returned collection has no effects on this
     * EventProvider.
     *
     * @param <L> Type of the listeners to return.
     * @param listenerClass The class representing the event for which the
     *            listeners should be retrieved.
     * @return A collection of listeners that should be notified about the event
     *         represented by the given listener class.
     * @throws IllegalArgumentException If listenerClass is <code>null</code>.
     */
    public <L extends Listener> Stream<L> get(Class<L> listenerClass);

    /**
     * Removes all listeners which have been registered for the provided
     * listener class. Every listner's
     * {@link Listener#onUnregister(RegistrationEvent) onUnregister} method will
     * be called.
     *
     * <p>
     * If listenerClass is <code>null</code> this method returns with no effect.
     * </p>
     *
     * <p>
     * <b>Note on concurrency:</b> This method can safely be called from within
     * a listening method during event handling to remove listeners. This will
     * have no impact on the current event delegation process.
     * </p>
     *
     * @param <L> Type of the listeners to remove.
     * @param listenerClass The class representing the event for which the
     *            listeners should be removed
     */
    public <L extends Listener> void clearAll(Class<L> listenerClass);

    /**
     * Removes all registered listeners from this EventProvider. Every listner's
     * {@link Listener#onUnregister(RegistrationEvent) onUnregister} method will
     * be called.
     *
     * <p>
     * <b>Note on concurrency:</b> This method can safely be called from within
     * a listening method during event handling to remove all listeners. This
     * will have no impact on the current event delegation process.
     * </p>
     */
    public void clearAll();

    public boolean isSequential();

    @Override
    public void close();
}