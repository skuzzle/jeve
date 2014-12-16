package de.skuzzle.jeve.stores;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.RegistrationEvent;

class PriorityListenerStoreImpl extends AbstractListenerStore implements
        PriorityListenerStore {

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

    private static class SynchronizedStore extends
            AbstractSynchronizedListenerStore<PriorityListenerStore> implements
            PriorityListenerStore {

        private SynchronizedStore(PriorityListenerStore wrapped) {
            super(wrapped);
        }

        @Override
        public PriorityListenerStore synchronizedView() {
            return this;
        }

        @Override
        public <T extends Listener> void add(Class<T> listenerClass, T listener,
                int priority) {
            modify(() -> this.wrapped.add(listenerClass, listener, priority));
        }
    }

    private static final int DEFAULT_PRIORITY = 0;

    private final Map<Class<? extends Listener>, List<ListenerWrapper>> listenerMap;
    private final int defaultPriority;
    private SynchronizedStore synchView;

    /**
     * Creates a new PriorityListenerStore with a default priority of {@code 0}.
     */
    public PriorityListenerStoreImpl() {
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
    public PriorityListenerStoreImpl(int defaultPriority) {
        this.defaultPriority = defaultPriority;
        this.listenerMap = new HashMap<>();
    }

    @Override
    public synchronized PriorityListenerStore synchronizedView() {
        if (this.synchView == null) {
            this.synchView = new SynchronizedStore(this);
        }
        return this.synchView;
    }

    @Override
    protected <T> List<T> createListenerList(int sizeHint) {
        return new LinkedList<>();
    }

    @Override
    public <T extends Listener> Stream<T> get(Class<T> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        }

        final List<ListenerWrapper> listeners = this.listenerMap.getOrDefault(
                listenerClass, Collections.emptyList());

        final int sizeHint = listeners.size();
        return copyList(listenerClass,
                listeners.stream().map(w -> w.listener),
                sizeHint).stream();
    }

    @Override
    public <T extends Listener> void add(Class<T> listenerClass, T listener) {
        add(listenerClass, listener, this.defaultPriority);
    }

    @Override
    public <T extends Listener> void add(Class<T> listenerClass, T listener,
            int priority) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        } else if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }

        final List<ListenerWrapper> listeners = this.listenerMap.computeIfAbsent(
                listenerClass, key -> createListenerList());
        addSorted(listeners, listener, priority);
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
        final RegistrationEvent e = new RegistrationEvent(this, listenerClass);
        listener.onUnregister(e);
    }

    @Override
    public void clearAll() {
        this.listenerMap.forEach((k, v) -> {
            final Iterator<ListenerWrapper> it = v.iterator();
            while (it.hasNext()) {
                removeInternal(k, it, it.next());
            }
        });
    }

    @Override
    public <T extends Listener> void clearAll(Class<T> listenerClass) {
        final List<ListenerWrapper> listeners = this.listenerMap.get(listenerClass);
        if (listeners != null) {
            final Iterator<ListenerWrapper> it = listeners.iterator();
            while (it.hasNext()) {
                removeInternal(listenerClass, it, it.next());
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
