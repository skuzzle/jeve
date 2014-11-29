package de.skuzzle.jeve.stores;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Stream;

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
 * constructor.
 *
 * <p>
 * Performance notes: This store uses a {@link HashMap} of {@link LinkedList
 * LinkedLists} to manage the Listeners and inserts Listeners sorted by priority
 * upon registering. Thus, adding a Listener as well as removing one performs in
 * {@code O(n)}, where {@code n} is the number of Listeners registered for the
 * class for which the Listener should be added/removed. {@link #get(Class)}
 * performs in {@code O(1)}.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public class PriorityListenerStore extends AbstractListenerStore implements ListenerStore {

    private static class ListenerWrapper {
        private final Object listener;
        private final int priority;

        private ListenerWrapper(Object listener, int priority) {
            this.listener = listener;
            this.priority = priority;
        }

        @Override
        public int hashCode() {
            return 31 * this.listener.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj != null &&
                    obj instanceof ListenerWrapper &&
                    this.listener.equals(((ListenerWrapper) obj).listener);
        }

        @Override
        public String toString() {
            return this.listener.toString();
        }
    }

    private static final int DEFAULT_PRIORITY = 0;

    private final Map<Class<? extends Listener>, List<ListenerWrapper>> listenerMap;
    private final int defaultPriority;

    /**
     * Creates a new PriorityListenerStore with a default priority of {@code 0}.
     */
    public PriorityListenerStore() {
        this(DEFAULT_PRIORITY);
    }

    /**
     * Creates a new PriorityListenerStore with the given
     * {@code defaultPriority}. That value will be used when registering
     * listeners with the overload of {@link #add(Class, Listener)}
     * which does not take a priority.
     *
     * @param defaultPriority Default value when adding Listeners without
     *            explicitly specifying a priority.
     */
    public PriorityListenerStore(int defaultPriority) {
        this.defaultPriority = defaultPriority;
        this.listenerMap = new HashMap<>();
    }

    @Override
    public <T extends Listener> Stream<T> get(Class<T> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        }

        synchronized (this.listenerMap) {
            final List<ListenerWrapper> listeners = this.listenerMap.get(listenerClass);
            final int sizeHint = listeners == null
                    ? 0
                    : listeners.size();

            return copyList(listenerClass,
                    nullSafeStream(listeners).map(w -> w.listener),
                    sizeHint).stream();
        }
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
    public <T extends Listener> void add(Class<T> listenerClass, T listener) {
        add(listenerClass, listener, this.defaultPriority);
    }

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
            int priority) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }

        synchronized (this.listenerMap) {
            final List<ListenerWrapper> listeners = this.listenerMap.computeIfAbsent(
                    listenerClass, key -> new LinkedList<>());
            addSorted(listeners, listener, priority);
        }
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onRegister(e);
    }

    private <L extends Listener> void addSorted(List<ListenerWrapper> listeners,
            L listener, int listenerPriority) {
        final ListIterator<ListenerWrapper> it = listeners.listIterator();
        while (it.hasNext()) {
            final ListenerWrapper next = it.next();

            // '<=' important here
            if (listenerPriority < next.priority) {
                it.set(new ListenerWrapper(listener, listenerPriority));
                it.add(next);
                return;
            }
        }
        it.add(new ListenerWrapper(listener, listenerPriority));
    }

    @Override
    public <T extends Listener> void remove(Class<T> listenerClass, T listener) {
        if (listenerClass == null || listener == null) {
            return;
        }

        synchronized (this.listenerMap) {
            final List<ListenerWrapper> listeners = this.listenerMap.get(listenerClass);
            if (listeners == null) {
                return;
            }
            // HINT: priority irrelevant here
            final ListenerWrapper key = new ListenerWrapper(listener, 0);

            listeners.remove(key);
            if (listeners.isEmpty()) {
                this.listenerMap.remove(listenerClass);
            }
        }
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onUnregister(e);
    }

    @Override
    public void clearAll() {
        synchronized (this.listenerMap) {
            this.listenerMap.forEach((k, v) -> {
                final Iterator<ListenerWrapper> it = v.iterator();
                while (it.hasNext()) {
                    removeInternal(k, it, it.next());
                }
            });
        }
    }

    @Override
    public <T extends Listener> void clearAll(Class<T> listenerClass) {
        synchronized (this.listenerMap) {
            final List<ListenerWrapper> listeners = this.listenerMap.get(listenerClass);
            if (listeners != null) {
                final Iterator<ListenerWrapper> it = listeners.iterator();
                while (it.hasNext()) {
                    removeInternal(listenerClass, it, it.next());
                }
            }
        }
    }

    /**
     * Internal method for removing a single listener and notifying it about the
     * removal. Prior to calling this method, the passed iterators
     * {@link Iterator#hasNext() hasNext} method must hold <code>true</code>.
     *
     * @param <T> Type of the listener to remove
     * @param listenerClass The class of the listener to remove.
     * @param it Iterator which provides the next listener to remove.
     * @param next The current wrapper element
     */
    protected <T extends Listener> void removeInternal(Class<T> listenerClass,
            Iterator<ListenerWrapper> it, ListenerWrapper next) {
        final T listener = listenerClass.cast(next.listener);
        it.remove();
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onUnregister(e);
    }

    @Override
    public void close() {
        this.clearAll();
    }

    @Override
    public String toString() {
        return this.listenerMap.toString();
    }

    @Override
    public boolean isSequential() {
        return false;
    }
}
