package de.skuzzle.jeve;

import java.io.Closeable;
import java.util.stream.Stream;

/**
 * Allows to register and unregister {@link Listener Listeners} for certain
 * listener classes and supplies {@code Listeners} to an {@link EventProvider}.
 * Every EventProvider implementation needs a reference to a ListenerStore which
 * manages its Listeners. It is allowed for different EventProviders to share a
 * single ListenerStore instance.
 *
 * <p>
 * When dispatching an {@link Event}, the EventProvider will query
 * {@link #get(Class)} with the respective {@link Event#getListenerClass()
 * listener class} of the event to dispatch. This method returns a
 * {@link Stream} of Listeners which were registered for that listener class
 * using {@link #add(Class, Listener)}. A ListenerStore is said to be
 * <b>sequential</b>, iff its {@code get} method returns the registered
 * Listeners in the exact order in which they have been registered (FIFO). When
 * using a non-sequential store with a sequential EventProvider, dispatching
 * with that provider immediately turns non-sequential too.
 * </p>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * For general purpose use with different kinds of EventProviders, the
 * ListenerStore implementation <em>must</em> be thread-safe. For example, an
 * asynchronous EventProvider could dispatch two Events concurrently, where
 * Listeners for both access the ListenerStore while being notified (E.g. they
 * remove themselves from the store for not being notified again).
 * </p>
 *
 * <h2>Closing</h2>
 * <p>
 * {@link #close() Closing} the ListenerStore will remove all registered
 * Listeners. Implementations may perform additional tasks. The ListenerStore is
 * automatically closed when an EventProvider which uses the store is closed.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
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
     * {@link #add(Class, Listener)} for the given listener class.
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

    /**
     * States whether this ListenerStore implementation is sequential. This is
     * the case if, and only if the Stream returned by {@link #get(Class)}
     * returns the registered Listeners in FIFO order regarding their time of
     * registration.
     *
     * @return Whether this store is sequential.
     */
    public boolean isSequential();

    /**
     * Removes all registered Listeners from this store (
     * {@link Listener#onUnregister(RegistrationEvent)} will be called on each
     * Listener). Implementors may also release additional resource held by
     * their implementations.
     *
     * <p>
     * <b>Note:</b> This method is automatically called upon closing an
     * {@link EventProvider} which uses this store.
     * </p>
     */
    @Override
    public void close();
}