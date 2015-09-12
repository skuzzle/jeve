package de.skuzzle.jeve;

import java.util.stream.Stream;

import de.skuzzle.jeve.annotation.ListenerKind;
import de.skuzzle.jeve.stores.AbstractSynchronizedListenerStore;
import de.skuzzle.jeve.stores.PriorityListenerStore;

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
 * <h2>Concurrency Safety</h2>
 * <p>
 * In contrast to the aspect of thread safety, <em>concurrency safety</em> also
 * kicks in on single threaded event dispatching scenarios. In the context of
 * listener stores, this means that the Stream returned by {@link #get(Class)}
 * must <b>not</b> be backed by the internal collection which is also modified
 * through {@link #add(Class, Listener) add} and
 * {@link #remove(Class, Listener) remove}. This guarantees a graceful and
 * deterministic behavior of synchronous event dispatching. While an event is
 * dispatched, the following conditions <b>must</b> always hold:
 * </p>
 * <ul>
 * <li>new listeners can be registered to this store, but they will first be
 * notified during the next dispatch action</li>
 * <li>existing listeners can be removed from this store, but they will
 * nevertheless be notified during the current dispatch action</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>
 * For general purpose use with different kinds of EventProviders, the
 * ListenerStore implementation <b>must</b> be thread-safe. For example, an
 * asynchronous EventProvider could dispatch two Events concurrently, where
 * Listeners for both access the ListenerStore while being notified (E.g. they
 * remove themselves from the store for not being notified again). For
 * performance reasons, stores should always be implemented without
 * synchronization and instead return a synchronized view of themselves with
 * {@link #synchronizedView()}. See {@link AbstractSynchronizedListenerStore}
 * for information on how to properly implement this.
 * </p>
 *
 * <h2>Closing</h2>
 * <p>
 * {@link #close() Closing} the ListenerStore will remove all registered
 * Listeners. Implementations may perform additional tasks. The ListenerStore is
 * automatically closed when an EventProvider which uses the store is closed.
 * </p>
 *
 * <h2>General Implementation Note</h2>
 * <p>
 * jeve makes excessive use of java's generics to provide as much compile time
 * type checks as possible. When creating an EventProvider, it will always be
 * parameterized with the type of the store it uses. This is to make API
 * extensions to the {@link ListenerStore} interface (like for example the
 * {@link PriorityListenerStore} does) accessible to the caller of
 * {@link EventProvider#getListenerSource()}.
 * </p>
 * <p>
 * In order to be able to create an EventProvider with a certain ListenerStore
 * implementation but also with that implementation's
 * {@linkplain #synchronizedView()}, the {@code synchronizedView's} return type
 * must be adjusted to be compatible with the actual implementation. For
 * example, if the {@code synchronizedView} method on a
 * {@linkplain PriorityListenerStore} would be declared to return the type
 * {@code ListenerStore}, then
 * </p>
 *
 * <pre>
 * EventProvider.configure().store(new PriorityListenerStore()).create();
 * </pre>
 *
 * and
 *
 * <pre>
 * EventProvider.configure()
 *         .store(new PriorityListenerStore().synchronizedView())
 *         .create();
 * </pre>
 * <p>
 * would yield two incompatible EventProvider types (the first is of type
 * {@code EventProvider<PriorityListenerStore>} and the second of type
 * {@code EventProvider<ListenerStore>}.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public interface ListenerStore extends ListenerSource {

    @Override
    public ListenerStore synchronizedView();

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
     * <b>Note on concurrency:</b>
     * This method can safely be called from within a listening method during
     * event handling to add a listener. This will have no impact on the current
     * event delegation process.
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
     * <p>
     * Adds the given object for all listener classes it implements. This
     * recursively traverses super classes and super interfaces of the given
     * listener's class. When encountering a super interface X which
     * </p>
     * <ol>
     * <li>is not {@code Listener.class} but</li>
     * <li>can be assigned to {@code Listener.class} and</li>
     * <li>is not annotated with {@link ListenerKind#TAGGING}</li>
     * </ol>
     * <p>
     * then the given listener will be registered for X. Otherwise, all super
     * interfaces of X are processed in the same way. When all super interfaces
     * have been processed, the same process is recursively applied to the super
     * class of the given listener if it has one. The <em>"otherwise"</em>
     * implies, that the inheritance hierarchy of classes which have already
     * been recognized as target listener class will not be traversed. If the
     * same listener class is encountered multiple times while traversing the
     * hierarchy, the listener will still only be registered once for that
     * class.
     * </p>
     * <p>
     * Note that for each class the listener is being added its
     * {@link Listener#onRegister(RegistrationEvent) onRegister} method is
     * called.
     * </p>
     *
     * @param <L> Type of the listener.
     * @param listener The listener to add.
     * @since 3.0.0
     */
    public <L extends Listener> void add(L listener);

    /**
     * Removes the given object for all listener classes it implements. See
     * {@link #add(Listener)} to learn how these classes are collected from the
     * given listener. If the same listener has been added multiple times for
     * the same listener class, only one reference will be removed. This is
     * compatible with the definition of {@link #remove(Class, Listener)}, which
     * also only removes one reference of the listener.
     *
     * <p>
     * Note that for each class the listener is being removed its
     * {@link Listener#onUnregister(RegistrationEvent) onUnregister} method is
     * called.
     * </p>
     *
     * @param <L> Type of the listener.
     * @param listener The listener to remove.
     * @since 3.0.0
     */
    public <L extends Listener> void remove(L listener);

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
     * <p>
     * <b>Note on concurrency:</b> The returned Stream will not be backed by the
     * internal list which is also modified via {@link #add(Class, Listener)} or
     * {@link #remove(Class, Listener)}. This allows to add or remove listeners
     * to this store while the Stream is being iterated.
     * </p>
     *
     * @param <L> Type of the listeners to return.
     * @param listenerClass The class representing the event for which the
     *            listeners should be retrieved.
     * @return A collection of listeners that should be notified about the event
     *         represented by the given listener class.
     * @throws IllegalArgumentException If listenerClass is <code>null</code>.
     */
    @Override
    public <L extends Listener> Stream<L> get(Class<L> listenerClass);

    /**
     * Removes all listeners which have been registered for the provided
     * listener class. Every listner's
     * {@link Listener#onUnregister(RegistrationEvent) onUnregister} method will
     * be called. The actual order in which listeners are removed is undefined.
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
     * be called. The actual order in which listeners are removed is undefined.
     *
     * <p>
     * <b>Note on concurrency:</b> This method can safely be called from within
     * a listening method during event handling to remove all listeners. This
     * will have no impact on the current event delegation process.
     * </p>
     */
    public void clearAll();

    /**
     * Removes all registered Listeners from this store (
     * {@link Listener#onUnregister(RegistrationEvent)} will be called on each
     * Listener). Implementors may also release additional resource held by
     * their implementations.
     */
    @Override
    public void close();
}
