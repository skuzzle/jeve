package de.skuzzle.jeve.stores;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.RegistrationEvent;

public final class DefaultListenerStore implements SequentialListenerStore {

    /** Holds the listener classes mapped to listener instances */
    protected final Map<Class<? extends Listener>, List<Object>> listeners;

    public DefaultListenerStore() {
        this.listeners = new HashMap<>();
    }

    @Override
    public <T extends Listener> void add(Class<T> listenerClass, T listener) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        synchronized (this.listeners) {
            List<Object> listeners = this.listeners.get(listenerClass);
            if (listeners == null) {
                listeners = new LinkedList<>();
                this.listeners.put(listenerClass, listeners);
            }
            listeners.add(listener);
        }
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onRegister(e);
    }

    @Override
    public <T extends Listener> void remove(Class<T> listenerClass, T listener) {
        if (listenerClass == null || listener == null) {
            return;
        }
        synchronized (this.listeners) {
            final List<Object> listeners = this.listeners.get(listenerClass);
            if (listeners == null) {
                return;
            }
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                this.listeners.remove(listenerClass);
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
        synchronized (this.listeners) {
            final List<Object> listeners = this.listeners.get(listenerClass);
            if (listeners == null) {
                return Collections.<T> emptyList().stream();
            }
            return listeners.stream().map(obj -> listenerClass.cast(obj));
        }
    }

    @Override
    public <T extends Listener> void clearAll(Class<T> listenerClass) {
        synchronized (this.listeners) {
            final List<Object> listeners = this.listeners.get(listenerClass);
            if (listeners != null) {
                final Iterator<Object> it = listeners.iterator();
                while (it.hasNext()) {
                    this.removeInternal(listenerClass, it);
                }
                this.listeners.remove(listenerClass);
            }
        }
    }

    @Override
    public void clearAll() {
        synchronized (this.listeners) {
            this.listeners.forEach((k, v) -> {
                final Iterator<Object> it = v.iterator();
                while (it.hasNext()) {
                    removeInternal(k, it);
                }
            });
            this.listeners.clear();
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
     */
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
        return this.listeners.toString();
    }
}
