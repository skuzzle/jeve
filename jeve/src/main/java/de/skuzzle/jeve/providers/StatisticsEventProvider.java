package de.skuzzle.jeve.providers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import de.skuzzle.jeve.DefaultDispatchable;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;
import de.skuzzle.jeve.invoke.EventInvocationFactory;

/**
 * EventProvider which delegates all method calls to a wrapped instance and
 * counts all dispatch actions.
 *
 * @param <P> The type of the wrapped provider.
 * @author Simon Taddiken
 * @since 2.0.0
 */
public class StatisticsEventProvider<P extends EventProvider>
        implements EventProvider {

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

    @Override
    public ListenerSource getListenerSource() {
        return this.wrapped.getListenerSource();
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
        this.wrapped.dispatch(event, bc);
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc, ExceptionCallback ec) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }

        increment(this.notifications, event.getListenerClass());
        this.wrapped.dispatch(event, bc, ec);
    }

    @Override
    public void dispatch(DefaultDispatchable event) {
        this.wrapped.dispatch(event);
    }

    @Override
    public void close() {
        this.wrapped.close();
        this.notifications.clear();
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
    public void setInvocationFactory(EventInvocationFactory factory) {
        this.wrapped.setInvocationFactory(factory);
    }

    @Override
    public boolean isSequential() {
        return this.wrapped.isSequential();
    }
}
