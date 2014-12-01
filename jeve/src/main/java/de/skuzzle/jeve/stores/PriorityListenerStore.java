package de.skuzzle.jeve.stores;

import java.util.HashMap;
import java.util.LinkedList;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.RegistrationEvent;

/**
 * Non-sequential {@link ListenerStore} implementation which provides Listener
 * prioritization upon registering. When registering a {@link Listener} with
 * this store, you may use an overload of {@link #add(Class, Listener, int) add}
 * to specify a priority for the Listener. The lower the priority, the sooner
 * the Listener will be notified when an Event is dispatched for its listener
 * class. When using the normal {@link #add(Class, Listener) add} method, the
 * Listener is assigned a default priority which may be specified in the
 * constructor. The public interface to this store is thread safe.
 *
 * <p>
 * Performance notes: This store uses a {@link HashMap} of {@link LinkedList
 * LinkedLists} to manage the Listeners and inserts Listeners sorted by priority
 * upon registering. Thus, adding a Listener as well as removing one performs in
 * {@code O(n)}, where {@code n} is the number of Listeners registered for the
 * class for which the Listener should be added/removed. The {@link #get(Class)
 * get} method retrieves the stored listeners from a map in {@code O(1)} but
 * then needs to create a copy of this list in order to avoid concurrency
 * problems. It therefore performs in {@code O(n)}.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public interface PriorityListenerStore extends ListenerStore {

    public static PriorityListenerStore create() {
        return new PriorityListenerStoreImpl();
    }

    public static PriorityListenerStore create(int defaultPriority) {
        return new PriorityListenerStoreImpl(defaultPriority);
    }

    /**
     * Adds a listener with the default priority specified in the constructor.
     * It will be notified for every event represented by the given listener
     * class. After registration, the listener's
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
     * @param <T> Type of the listener to add.
     * @param listenerClass The class representing the event(s) to listen on.
     * @param listener The listener to add.
     * @throws IllegalArgumentException If either listenerClass or listener
     *             argument is <code>null</code>.
     */
    @Override
    public <T extends Listener> void add(Class<T> listenerClass, T listener);

    /**
     * Adds a listener with the given priority. It will be notified for every
     * event represented by the given listener class. After registration, the
     * listener's {@link Listener#onRegister(RegistrationEvent) onRegister}
     * method gets called to notify the listener about being added to a new
     * parent. The {@code onRegister} method is not subject to the dispatching
     * strategy implemented by this {@link EventProvider} and is called from the
     * current thread.
     *
     * <p>
     * <b>Note on concurrency:</b> This method can safely be called from within
     * a listening method during event handling to add a listener. This will
     * have no impact on the current event delegation process.
     * </p>
     *
     * @param <T> Type of the listener to add.
     * @param listenerClass The class representing the event(s) to listen on.
     * @param listener The listener to add.
     * @param priority The priority of the new listener.
     * @throws IllegalArgumentException If either listenerClass or listener
     *             argument is <code>null</code>.
     */
    public <T extends Listener> void add(Class<T> listenerClass, T listener,
            int priority);

}
