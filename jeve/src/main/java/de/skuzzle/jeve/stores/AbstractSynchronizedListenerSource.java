package de.skuzzle.jeve.stores;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

public abstract class AbstractSynchronizedListenerSource<T extends ListenerSource>
        implements ListenerSource {

    /**
     * Represents an atomic write transaction. Actions within {@link #perform()}
     * will be executed in the context of a write lock when passed to the
     * {@link AbstractSynchronizedListenerStore#modify(Transaction)}.
     *
     * @author Simon Taddiken
     * @since 3.0.0
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
    /** The wrapped store */
    protected final T wrapped;

    protected AbstractSynchronizedListenerSource(T wrapped) {
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
     * @param <E> The result type.
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

    /**
     * {@inheritDoc}
     *
     * <p>
     * <b>Note:</b> This method should be implemented to return this store
     * itself.
     * </p>
     */
    @Override
    public abstract T synchronizedView();

    @Override
    public <L extends Listener> Stream<L> get(Class<L> listenerClass) {
        return read(() -> this.wrapped.get(listenerClass));
    }

    @Override
    public void close() {
        modify(() -> this.wrapped.close());
    }

}