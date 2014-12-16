package de.skuzzle.jeve.stores;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * Helper class for implementing the {@link ListenerStore#synchronizedView()}
 * method. This class provides a thread safe interface to the decorated store
 * which is passed in the constructor.
 *
 * @author Simon Taddiken
 * @param <T> The type of the decorated store.
 * @since 3.0.0
 */
public abstract class AbstractSynchronizedListenerStore<T extends ListenerStore>
        implements ListenerStore {

    /**
     * Represents an atomic write transaction. Actions within {@link #perform()}
     * will be executed in the context of a write lock when passed to the
     * {@link AbstractSynchronizedListenerStore#modify(Transaction)}.
     *
     * @author Simon Taddiken
     */
    @FunctionalInterface
    protected static interface Transaction {
        /**
         * Can be executed atomically by passing an instance of this interface
         * to {@link AbstractSynchronizedListenerStore#modify(Transaction)}.
         */
        public void perform();
    }

    protected final ReadWriteLock lock;
    protected final T wrapped;

    public AbstractSynchronizedListenerStore(T wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped is null");
        }
        this.wrapped = wrapped;
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Executes the given transaction within the context of a write lock.
     *
     * @param t The transaction to execute.
     */
    protected void modify(Transaction t) {
        try {
            this.lock.writeLock().lock();
            t.perform();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    /**
     * Executes the given supplier within the context of a read lock.
     *
     * @param sup The supplier.
     * @return The result of {@link Supplier#get()}.
     */
    protected <E> E read(Supplier<E> sup) {
        try {
            this.lock.readLock().lock();
            return sup.get();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public abstract T synchronizedView();

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
    public <L extends Listener> Stream<L> get(Class<L> listenerClass) {
        return read(() -> this.wrapped.get(listenerClass));
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

    @Override
    public void close() {
        modify(() -> this.wrapped.close());
    }
}
