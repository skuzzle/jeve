package de.skuzzle.jeve.stores;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

public abstract class AbstractSynchronizedListenerStore<T extends ListenerStore> implements
        ListenerStore {

    protected final ReadWriteLock lock;
    protected final T wrapped;

    public AbstractSynchronizedListenerStore(T wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped is null");
        }
        this.wrapped = wrapped;
        this.lock = new ReentrantReadWriteLock();
    }

    protected void modify(Runnable r) {
        try {
            this.lock.writeLock().lock();
            r.run();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

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
