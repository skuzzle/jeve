package de.skuzzle.jeve.providers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import de.skuzzle.jeve.DefaultTargetEvent;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * EventProvider which delegates all method calls to a wrapped instance and
 * counts all dispatch actions.
 *
 * @param <S> The type of the ListenerStore this provider uses.
 * @param <P> The type of the wrapped provider.
 * @author Simon Taddiken
 * @since 2.0.0
 */
public class StatisticsEventProvider<S extends ListenerStore, P extends EventProvider<S>>
        implements EventProvider<S> {

    private final Map<Class<? extends Listener>, Integer> notifications;
    private final P wrapped;

    /**
     * Creates a new StatisticsEventProvider which counts dispatch action of the
     * given {@link EventProvider}.
     *
     * @param wrapped The provider to wrap.
     */
    public StatisticsEventProvider(P wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped is null");
        }
        this.wrapped = wrapped;
        this.notifications = Collections.synchronizedMap(new HashMap<>());
    }

    private <K> void increment(Map<K, Integer> map, K key) {
        map.compute(key, (k, i) -> i == null
                ? 1
                : i + 1);
    }

    /**
     * Gets the underlying EventProvider which is wrapped by this
     * StatisticsEventProvider.
     *
     * @return The wrapped provider.
     */
    public P getWrapped() {
        return this.wrapped;
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
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }

        increment(this.notifications, event.getListenerClass());
        event.setListenerStore(listeners());
        this.wrapped.dispatch(event, bc);
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc, ExceptionCallback ec) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }

        increment(this.notifications, event.getListenerClass());
        event.setListenerStore(listeners());
        this.wrapped.dispatch(event, bc, ec);
    }

    @Override
    public <L extends Listener, E extends DefaultTargetEvent<?, E, L>> void dispatch(
            E event) {
        this.wrapped.dispatch(event);
    }

    @Override
    public void close() {
        this.wrapped.close();
        this.notifications.clear();
    }

    @Override
    public S listeners() {
        return this.wrapped.listeners();
    }

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
        return this.wrapped.isSequential();
    }
}
