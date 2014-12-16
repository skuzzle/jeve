package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.RegistrationEvent;

class DefaultListenerStoreImpl extends AbstractListenerStore implements
        DefaultListenerStore {

    private static class SynchronizedStore extends
            AbstractSynchronizedListenerStore<DefaultListenerStore> implements
            DefaultListenerStore {

        private SynchronizedStore(DefaultListenerStore wrapped) {
            super(wrapped);
        }

        @Override
        public DefaultListenerStore synchronizedView() {
            return this;
        }
    }

    /** Holds the listener classes mapped to listener instances */
    protected final Map<Class<? extends Listener>, List<Object>> listenerMap;

    private SynchronizedStore synchView;

    /**
     * Creates a new DefaultListenerStore.
     */
    public DefaultListenerStoreImpl() {
        this.listenerMap = new HashMap<>();
    }

    @Override
    public synchronized DefaultListenerStore synchronizedView() {
        if (this.synchView == null) {
            this.synchView = new SynchronizedStore(this);
        }
        return this.synchView;
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
        this.listenerMap.computeIfAbsent(listenerClass, key -> createListenerList())
                .add(listener);
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onRegister(e);
    }

    @Override
    public <T extends Listener> void remove(Class<T> listenerClass, T listener) {
        if (listenerClass == null || listener == null) {
            return;
        }
        final List<Object> targets = this.listenerMap.get(listenerClass);
        if (targets == null) {
            return;
        }
        targets.remove(listener);
        if (targets.isEmpty()) {
            this.listenerMap.remove(listenerClass);
        }
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onUnregister(e);
    }

    @Override
    public <T extends Listener> Stream<T> get(Class<T> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass");
        }
        final List<Object> targets = this.listenerMap.getOrDefault(listenerClass,
                Collections.emptyList());
        final int sizeHint = targets.size();
        return copyList(listenerClass, targets.stream(), sizeHint).stream();
    }

    @Override
    public <T extends Listener> void clearAll(Class<T> listenerClass) {
        final List<Object> targets = this.listenerMap.get(listenerClass);
        clearAll(listenerClass, targets, true);
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
        this.listenerMap.keySet().forEach(listenerClass -> {
            final List<Object> targets = this.listenerMap.get(listenerClass);
            clearAll(listenerClass, targets, false);
        });
        this.listenerMap.clear();
    }

    /**
     * Internal method for removing a single listener and notifying it about the
     * removal. Prior to calling this method, the passed iterators
     * {@link Iterator#hasNext() hasNext} method must hold <code>true</code>.
     *
     * @param <T> Type of the listener to remove
     * @param listenerClass The class of the listener to remove.
     * @param it Iterator which provides the next listener to remove.
     * @deprecated Since 3.0.0 - Method not used anymore. Replaced by
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
