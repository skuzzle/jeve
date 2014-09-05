package de.skuzzle.jeve;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * EventProvider which delegates all method calls to a wrapped instance but
 * counts all notifications.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public class StatisticsEventProvider implements EventProvider {

    private final EventProvider wrapped;
    private final Map<Class<? extends Listener>, Integer> notifications;

    StatisticsEventProvider(EventProvider wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped is null");
        }
        this.wrapped = wrapped;
        this.notifications = Collections.synchronizedMap(new HashMap<>());
    }

    private <K> void increment(Map<K, Integer> map, K key) {
        Integer val = map.get(key);
        if (val == null) {
            val = 0;
        }
        map.put(key, val + 1);
    }

    /**
     * Gets a map containing statistics about how often a certain kind of
     * listener has been notified. The returned map is read only and backed by
     * the internal map. Thus, once retrieved the map, its contents may change
     * over time.
     *
     * @return A read only map view of the current notification statistics.
     */
    public Map<Class<? extends Listener>, Integer> getNotificationStatistics() {
        return Collections.unmodifiableMap(this.notifications);
    }

    @Override
    public <T extends Listener> void addListener(Class<T> listenerClass, T listener) {
        this.wrapped.addListener(listenerClass, listener);
    }

    @Override
    public <T extends Listener> void removeListener(Class<T> listenerClass, T listener) {
        this.wrapped.removeListener(listenerClass, listener);
    }

    @Override
    public boolean canDispatch() {
        return this.wrapped.canDispatch();
    }

    @Override
    public void clearAllListeners() {
        this.wrapped.clearAllListeners();
    }

    @Override
    public <T extends Listener> void clearAllListeners(Class<T> listenerClass) {
        this.wrapped.clearAllListeners(listenerClass);
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }

        increment(this.notifications, event.getListenerClass());
        event.setEventProvider(this);
        this.wrapped.dispatch(event, bc);
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc, ExceptionCallback ec) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }

        increment(this.notifications, event.getListenerClass());
        event.setEventProvider(this);
        this.wrapped.dispatch(event, bc, ec);
    }

    @Override
    public <T extends Listener> Collection<T> getListeners(Class<T> listenerClass) {
        return this.wrapped.getListeners(listenerClass);
    }

    @Override
    public boolean isSequential() {
        return this.wrapped.isSequential();
    }

    @Override
    public void setExceptionCallback(ExceptionCallback ec) {
        this.wrapped.setExceptionCallback(ec);
    }

    @Override
    public void close() {
        this.wrapped.close();
    }
}
