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
 * lowest assigned int value will be notified first. Therefore, this provider is
 * not generally sequential as by the specification of EventProviders but it is
 * sequential per priority level. That is, Listeners with the same priority are
 * notified in the order in which they have been registered with the provider.
 *
 * <p>
 * This provider wraps around another and delegates all methods to the wrapped
 * instance. Wrapping a non-sequential provider is not forbidden but also makes
 * little sense, as sorting the providers by priority would have no effect if
 * their notification order is undefined anyhow.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public class PriorityEventProvider implements EventProvider {

    private final class SortingFilter implements ListenerFilter {

        @Override
        public <L extends Listener> void preprocess(EventProvider parent,
                Class<L> listenerClass, List<L> listeners) {

            synchronized (PriorityEventProvider.this.priorityMap) {
                if (PriorityEventProvider.this.resortSet.remove(listenerClass)) {
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
    private final Set<Class<? extends Listener>> resortSet;
    private final EventProvider wrapped;
    private final int defaultPriority;
    private final ListenerFilter sortingFilter;

    /**
     * Creates a new PriorityEventProvider with a default priority of {@code 0}.
     *
     * @param wrapped The EventProvider which actually performs the dispatching.
     */
    public PriorityEventProvider(EventProvider wrapped) {
        this(wrapped, DEFAULT_PRIORITY);
    }

    /**
     * Creates a new PriorityEventProvider with the given
     * {@code defaultPriority}. That value will be used when registering
     * listeners with the overload of {@link #addListener(Class, Listener)}
     * which does not take a priority.
     *
     * @param wrapped The EventProvider which actually performs the dispatching.
     * @param defaultPriority Default value when adding Listeners without
     *            explicitly specifying a priority.
     */
    public PriorityEventProvider(EventProvider wrapped, int defaultPriority) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped is null");
        }
        this.wrapped = wrapped;
        this.sortingFilter = new SortingFilter();
        this.defaultPriority = defaultPriority;
        this.priorityMap = new HashMap<>();
        this.resortSet = new HashSet<>();
        wrapped.setListenerFilter(this.sortingFilter);
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
    public <T extends Listener> void addListener(Class<T> listenerClass, T listener) {
        this.addListener(listenerClass, listener, this.defaultPriority);
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
    public <T extends Listener> void addListener(Class<T> listenerClass, T listener,
            int priority) {
        this.wrapped.addListener(listenerClass, listener);
        synchronized (this.priorityMap) {
            this.priorityMap.put(listener, priority);
            this.resortSet.add(listenerClass);
        }
    }

    /**
     * Given that {@code second} is already registered with this provider, this
     * method adds the new listener {@code first} with a higher priority so it
     * is notified before the first one. If {@code second} is assigned the
     * highest possible priority, this method throws an exception.
     *
     * <p>
     * If {@code second} is not already registered, the new Listener will be
     * added with the default priority specified in the constructor.
     * </p>
     *
     * @param <T> Type of the listener to add.
     * @param listenerClass The class representing the event(s) to listen on.
     * @param first The listener to add.
     * @param second The listener which is already registered.
     * @throws IllegalArgumentException If either listenerClass or listener
     *             argument is <code>null</code>.
     * @see #addListener(Class, Listener, int)
     */
    public <T extends Listener> void addListenerBefore(Class<T> listenerClass, T first,
            T second) {

        synchronized (this.priorityMap) {
            final Integer secondPriority = this.priorityMap.get(second);
            int priority = this.defaultPriority;
            if (secondPriority != null) {
                if (secondPriority.equals(Integer.MIN_VALUE)) {
                    throw new IllegalStateException(String.format(
                            "can't add %s before %s: No higher priority than Integer.MIN_VALUE available",
                            first, second));
                }
                priority = secondPriority - 1;
            }
            this.wrapped.addListener(listenerClass, first);
            this.priorityMap.put(first, priority);
            this.resortSet.add(listenerClass);
        }
    }

    /**
     * Given that {@code first} is already registered with this provider, this
     * method adds the new listener {@code second} with a lower priority so it
     * is notified after the first one. If {@code first} is assigned the lowest
     * possible priority, this method throws an exception.
     *
     * <p>
     * If {@code first} is not already registered, the new Listener will be
     * added with the default priority specified in the constructor.
     * </p>
     *
     * @param <T> Type of the listener to add.
     * @param listenerClass The class representing the event(s) to listen on.
     * @param first The listener to add.
     * @param second The listener which is already registered.
     * @throws IllegalArgumentException If either listenerClass or listener
     *             argument is <code>null</code>.
     * @see #addListener(Class, Listener, int)
     */
    public <T extends Listener> void addListenerAfter(Class<T> listenerClass, T first,
            T second) {

        synchronized (this.priorityMap) {
            final Integer firstPriority = this.priorityMap.get(first);
            int priority = this.defaultPriority;
            if (firstPriority != null) {
                if (firstPriority.equals(Integer.MAX_VALUE)) {
                    throw new IllegalStateException(String.format(
                            "can't add %s after %s: No lower priority than Integer.MAX_VALUE available",
                            second, first));
                }
                priority = firstPriority - 1;
            }
            this.wrapped.addListener(listenerClass, second);
            this.priorityMap.put(second, priority);
            this.resortSet.add(listenerClass);
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
        return this.wrapped.getListeners(listenerClass);
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
            BiConsumer<L, E> bc) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }
        event.setEventProvider(this);
        this.wrapped.dispatch(event, bc);
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(E event,
            BiConsumer<L, E> bc, ExceptionCallback ec) {
        if (event == null) {
            throw new IllegalArgumentException("event is null");
        }
        event.setEventProvider(this);
        this.wrapped.dispatch(event, bc, ec);
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
        return false;
    }

    @Override
    public void close() {
        this.wrapped.close();
    }
}
