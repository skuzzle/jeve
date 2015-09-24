package de.skuzzle.jeve.stores;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * Helper class for implementing the {@link ListenerStore#synchronizedView()}
 * method. This class provides a thread safe interface to the decorated store
 * which is passed in the constructor.
 *
 * <h2>Implementation Note</h2>
 * <p>
 * The implementation of this class should always be assignment compatible with
 * the store it decorates. Thus in general, you would define an interface for
 * your store which will then be implemented by the actual store implementation
 * and the implementation of this class (which is typically a private class of
 * your store).
 * </p>
 *
 * @author Simon Taddiken
 * @param <T> The type of the decorated store.
 * @since 3.0.0
 */
public abstract class AbstractSynchronizedListenerStore<T extends ListenerStore>
        extends AbstractSynchronizedListenerSource<T>
        implements ListenerStore {

    /**
     * Creates a new AbstractSynchronizedListenerStore which wraps the given
     * store.
     *
     * @param wrapped The wrapped store.
     */
    protected AbstractSynchronizedListenerStore(T wrapped) {
        super(wrapped);
    }

    @Override
    public <L extends Listener> void add(Class<L> listenerClass, L listener) {
        modify(() -> this.wrapped.add(listenerClass, listener));
    }

    @Override
    public <L extends Listener> void add(L listener) {
        modify(() -> this.wrapped.add(listener));
    }

    @Override
    public <L extends Listener> void remove(L listener) {
        modify(() -> this.wrapped.remove(listener));
    }

    @Override
    public <L extends Listener> void remove(Class<L> listenerClass, L listener) {
        modify(() -> this.wrapped.remove(listenerClass, listener));
    }

    @Override
    public <L extends Listener> void clearAll(Class<L> listenerClass) {
        modify(() -> this.wrapped.clearAll(listenerClass));
    }

    @Override
    public void clearAll() {
        modify(() -> this.wrapped.clearAll());
    }

    @Override
    public boolean isSequential() {
        return this.wrapped.isSequential();
    }
}
