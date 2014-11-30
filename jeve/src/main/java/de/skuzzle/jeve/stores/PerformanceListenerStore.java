package de.skuzzle.jeve.stores;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * Extension to the {@link DefaultListenerStore} which offers to enable higher
 * {@link #get(Class)} performance. The higher performance is achieved at the
 * cost of lowering the performance of {@link #remove(Class, Listener)} and
 * {@link #add(Class, Listener)}. The public interface to this store is thread
 * safe.
 *
 * <p>
 * Most applications have something like a setup phase in which listeners are
 * registered. After this phase, the listener store is rarely modified anymore,
 * but events will be fired frequently from now on. If this reflects your usage
 * scenario, this {@linkplain ListenerStore} implementation can offer higher
 * runtime performance.
 * </p>
 *
 * <p>
 * After instantiation, this store behaves exactly like the
 * {@link DefaultListenerStore}. After the registration phase of your
 * application is finished, you may call {@link #optimizeGet()} on this store.
 * This will replace all regular Lists which are used to store different
 * listeners with {@link CopyOnWriteArrayList CopyOnWriteArrayLists}. This
 * reduces the need to copy the list of retrieved Listeners upon
 * {@link #get(Class)}. The cost of copying is thereby moved to the
 * {@link #add(Class, Listener)} and {@link #remove(Class, Listener)} methods.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.1.0
 */
public class PerformanceListenerStore extends DefaultListenerStore {

    /** Locks access to {@link #optimized}. */
    protected final Object optimizeMutex = new Object();

    /** Whether {@link #optimizeGet()} has already been called. */
    protected boolean optimized;

    /** Auto {@link #optimizeGet()} on first call to {@link #get(Class)} */
    protected final boolean autoOptimize;

    /**
     * Creates a new PerformanceListenerStore.
     */
    public PerformanceListenerStore() {
        this(false);
    }

    /**
     * Creates a PerformanceListenerStore with optionally enabling auto
     * optimize. When auto optimize is enabled, {@link #optimizeGet()} will
     * automatically be called the first time {@link #get(Class)} is called.
     *
     * @param pAutoOptimize Whether to enable auto optimize.
     */
    public PerformanceListenerStore(boolean pAutoOptimize) {
        this.autoOptimize = pAutoOptimize;
    }

    /**
     * Whether auto optimization upon first call to {@link #get(Class)} has been
     * enabled in the constructor.
     *
     * @return Whether auto optimize is enabled.
     */
    public boolean isAutoOptimize() {
        return this.autoOptimize;
    }

    /**
     * Interchanges the performance characteristics of {@link #get(Class)} with
     * {@link #add(Class, Listener)} resp. {@link #remove(Class, Listener)}.
     * <p>
     * This will store all currently registered listeners into
     * {@link CopyOnWriteArrayList CopyOnWriteArrayLists}. Thus, after calling
     * this method, the {@code add} and {@code remove} methods will always copy
     * the internal array of the respective listener list. In return, the
     * {@link #get(Class)} method does not need to create a copy of the
     * retrieved listener list to achieve concurrency safety. Therefore, after
     * calling this method
     * </p>
     * <ul>
     * <li>{@code add} and {@code remove} perform in {@code O(n)}.</li>
     * <li>{@code clear}, {@code clearAll} and thus {@code close} perform in
     * {@code O(n²)}.</li>
     * <li>{@code get} performs in O(1).
     * </ul>
     * <p>
     * Where {@code n} is the number of listeners registered for a listener
     * class.
     * </p>
     *
     * <p>
     * This method may only be called once. Subsequent calls will have no
     * effect. The internal modifications done by this method can not be
     * reverted. Whether this method has already been called can be queried with
     * {@link #isOptimized()}.
     * </p>
     */
    public void optimizeGet() {
        synchronized (this.optimizeMutex) {
            if (this.optimized) {
                return;
            }
            this.optimized = true;
            synchronized (this.listenerMap) {
                for (final Entry<Class<? extends Listener>, List<Object>> e : this.listenerMap.entrySet()) {
                    final List<Object> cpy = new CopyOnWriteArrayList<>(e.getValue());
                    e.setValue(cpy);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * After {@link #optimizeGet()} has been called, this method will return
     * instances of {@link CopyOnWriteArrayList}.
     * </p>
     */
    @Override
    protected <T> List<T> createListenerList(int sizeHint) {
        if (this.optimized) {
            return new CopyOnWriteArrayList<>();
        }
        return super.createListenerList(sizeHint);
    }

    /**
     * Whether {@link #optimizeGet()} has been called on this store.
     *
     * @return Whether {@link #optimizeGet()} has been called on this store.
     */
    public boolean isOptimized() {
        synchronized (this.optimizeMutex) {
            return this.optimized;
        }
    }

    @Override
    public <T extends Listener> Stream<T> get(Class<T> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass");
        }
        if (this.autoOptimize) {
            optimizeGet();
        }
        synchronized (this.listenerMap) {
            final List<Object> targets = this.listenerMap.getOrDefault(listenerClass,
                    Collections.emptyList());

            synchronized (this.optimizeMutex) {
                if (this.optimized) {
                    return targets.stream().map(listener -> listenerClass.cast(listener));
                } else {
                    final int sizeHint = targets.size();
                    return copyList(listenerClass, targets.stream(), sizeHint).stream();
                }
            }
        }
    }
}
