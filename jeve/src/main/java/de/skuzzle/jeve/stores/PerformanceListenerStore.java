package de.skuzzle.jeve.stores;

import java.util.concurrent.CopyOnWriteArrayList;

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
 * @since 3.0.0
 */
public interface PerformanceListenerStore extends DefaultListenerStore {

    public static PerformanceListenerStore create() {
        return new PerformanceListenerStoreImpl();
    }

    public static PerformanceListenerStore withAutoOptimize() {
        return new PerformanceListenerStoreImpl(true);
    }

    /**
     * Whether auto optimization upon first call to {@link #get(Class)} has been
     * enabled in the constructor.
     *
     * @return Whether auto optimize is enabled.
     */
    public boolean isAutoOptimize();

    /**
     * Whether {@link #optimizeGet()} has been called on this store.
     *
     * @return Whether {@link #optimizeGet()} has been called on this store.
     */
    public boolean isOptimized();

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
    public void optimizeGet();
}
