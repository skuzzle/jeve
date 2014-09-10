package de.skuzzle.jeve;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * An {@link EventProvider} implementation which provides prioritization of
 * Listeners. When adding a Listener to this provider, you can specify an
 * integer priority for it. When dispatching an Event, those Listeners with the
 * lowest assigned int value will be notified first.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public class PriorityEventProvider implements EventProvider {

    private final class SortingFilter implements ListenerFilter {

        @Override
        public <L extends Listener> void preprocess(Class<L> listenerClass,
                List<L> listeners) {

            synchronized (PriorityEventProvider.this.priorityMap) {
                if (PriorityEventProvider.this.resortMap.remove(listenerClass)) {
                    Collections.sort(listeners, PriorityEventProvider.this::compare);
                }
            }
        }

        @Override
        public boolean isSequential() {
            return false;
        }
    }

    private static final int DEFAULT_PRIORITY = 0;

    private final Map<Listener, Integer> priorityMap;
    private final Set<Class<? extends Listener>> resortMap;
    private final EventProvider wrapped;
    private final int defaultPriority;
    private final ListenerFilter sortingFilter;

    public PriorityEventProvider(EventProvider wrapped) {
        this(wrapped, DEFAULT_PRIORITY);
    }

    public PriorityEventProvider(EventProvider wrapped, int defaultPriority) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped is null");
        }
        this.wrapped = wrapped;
        this.sortingFilter = new SortingFilter();
        this.defaultPriority = defaultPriority;
        this.priorityMap = new HashMap<>();
        this.resortMap = new HashSet<>();
    }

    private <L extends Listener> int compare(L listener1, L listener2) {
        final int priority1 = this.priorityMap.get(listener1);
        final int priority2 = this.priorityMap.get(listener2);
        return Integer.compare(priority1, priority2);
    }

    @Override
    public void setListenerFilter(ListenerFilter filter) {
        final ListenerFilter combined = CompoundListenerFilter.combineFilter(filter,
                this.sortingFilter);
        this.wrapped.setListenerFilter(combined);
    }

    @Override
    public <T extends Listener> void addListener(Class<T> listenerClass, T listener) {
        this.addListener(listenerClass, listener, this.defaultPriority);
    }

    public <T extends Listener> void addListener(Class<T> listenerClass, T listener,
            int priority) {
        this.wrapped.addListener(listenerClass, listener);
        synchronized (this.priorityMap) {
            this.priorityMap.put(listener, priority);
            this.resortMap.add(listenerClass);
        }
    }

    @Override
    public <T extends Listener> void removeListener(Class<T> listenerClass, T listener) {
        this.wrapped.removeListener(listenerClass, listener);
        synchronized (this.priorityMap) {
            this.priorityMap.remove(listener);
        }
    }


    @Override
    public <T extends Listener> Collection<T> getListeners(Class<T> listenerClass) {
        return null;
    }

    @Override
    public <T extends Listener> void clearAllListeners(Class<T> listenerClass) {
        this.wrapped.clearAllListeners(listenerClass);
    }

    @Override
    public void clearAllListeners() {
        this.wrapped.clearAllListeners();
        synchronized (this.priorityMap) {
            this.priorityMap.clear();
        }
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc) {}

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc, ExceptionCallback ec) {}

    @Override
    public boolean canDispatch() {
        return this.wrapped.canDispatch();
    }

    @Override
    public void setExceptionCallback(ExceptionCallback ec) {
        this.wrapped.setExceptionCallback(ec);
    }

    @Override
    public boolean isSequential() {
        return false;
    }

    @Override
    public void close() {
        this.wrapped.close();
    }
}
