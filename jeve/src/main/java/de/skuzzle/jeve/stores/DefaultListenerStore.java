package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.RegistrationEvent;

/**
 * Sequential {@link ListenerStore} implementation. This class implements the
 * default semantics of a ListenerStore with no additional features. The public
 * interface to this store is thread safe.
 *
 * <p>
 * Performance notes: This store uses a {@link HashMap} of {@link ArrayList
 * ArrayLists} to manage the Listeners. Thus, adding a Listener performs in
 * {@code O(1)} and removing in {@code O(n)} where {@code n} is the number of
 * Listeners registered for the class for which the Listener should be removed.
 * The {@link #get(Class) get} method retrieves the stored listeners from a map
 * in {@code O(1)} but then needs to create a copy of this list in order to
 * avoid concurrency problems. It therefore performs in {@code O(n)}.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public class DefaultListenerStore extends AbstractListenerStore {

    /** Holds the listener classes mapped to listener instances */
    protected final Map<Class<? extends Listener>, List<Object>> listenerMap;

    /**
     * Creates a new DefaultListenerStore.
     */
    public DefaultListenerStore() {
        this.listenerMap = new HashMap<>();
    }

    @Override
    protected <T> List<T> createListenerList(int sizeHint) {
        return new ArrayList<>(sizeHint);
    }

    @Override
    public <T extends Listener> void add(Class<T> listenerClass, T listener) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        synchronized (this.listenerMap) {
            this.listenerMap.computeIfAbsent(listenerClass, key -> createListenerList())
                    .add(listener);
        }
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onRegister(e);
    }

    @Override
    public <T extends Listener> void remove(Class<T> listenerClass, T listener) {
        if (listenerClass == null || listener == null) {
            return;
        }
        synchronized (this.listenerMap) {
            final List<Object> targets = this.listenerMap.get(listenerClass);
            if (targets == null) {
                return;
            }
            targets.remove(listener);
            if (targets.isEmpty()) {
                this.listenerMap.remove(listenerClass);
            }
        }
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onUnregister(e);
    }

    @Override
    public <T extends Listener> Stream<T> get(Class<T> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass");
        }
        synchronized (this.listenerMap) {
            final List<Object> targets = this.listenerMap.getOrDefault(listenerClass,
                    Collections.emptyList());
            final int sizeHint = targets.size();
            return copyList(listenerClass, targets.stream(), sizeHint).stream();
        }
    }

    @Override
    public <T extends Listener> void clearAll(Class<T> listenerClass) {
        synchronized (this.listenerMap) {
            final List<Object> targets = this.listenerMap.get(listenerClass);
            clearAll(listenerClass, targets, true);
        }
    }

    protected <T extends Listener> void clearAll(Class<T> listenerClass,
            List<Object> listeners, boolean removeFromMap) {
        if (listeners == null) {
            return;
        }

        // backwards iteration has higher removal performance on array
        // lists
        for (int i = listeners.size() - 1; i >= 0; --i) {
            final T listener = listenerClass.cast(listeners.remove(i));
            final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
            listener.onUnregister(e);
        }

        if (removeFromMap) {
            this.listenerMap.remove(listenerClass);
        }
    }

    @Override
    public void clearAll() {
        synchronized (this.listenerMap) {
            this.listenerMap.keySet().forEach(listenerClass -> {
                final List<Object> targets = this.listenerMap.get(listenerClass);
                clearAll(listenerClass, targets, false);
            });
            this.listenerMap.clear();
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
     * @deprecated Since 2.1.0 - Method not used anymore. Replaced by
     *             {@link #clearAll(Class, List, boolean)}.
     */
    @Deprecated
    protected <T extends Listener> void removeInternal(Class<T> listenerClass,
            Iterator<Object> it) {
        final Object next = it.next();
        final T listener = listenerClass.cast(next);
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
        return true;
    }
}
