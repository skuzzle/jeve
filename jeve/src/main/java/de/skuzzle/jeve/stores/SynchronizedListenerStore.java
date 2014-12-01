package de.skuzzle.jeve.stores;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

public abstract class SynchronizedListenerStore<T extends ListenerStore> implements
        ListenerStore {

    protected final ReadWriteLock lock;
    protected final T wrapped;

    public SynchronizedListenerStore(T wrapped) {
        if (wrapped == null) {
            throw new IllegalArgumentException("wrapped is null");
        }
        this.wrapped = wrapped;
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public abstract T synchronizedView();

    @Override
    public <L extends Listener> void add(Class<L> listenerClass, L listener) {
        try {
            this.lock.writeLock().lock();
            this.wrapped.add(listenerClass, listener);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public <L extends Listener> void add(L listener) {
        try {
            this.lock.writeLock().lock();
            this.wrapped.add(listener);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public <L extends Listener> void remove(L listener) {
        try {
            this.lock.writeLock().lock();
            this.wrapped.remove(listener);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public <L extends Listener> void remove(Class<L> listenerClass, L listener) {
        try {
            this.lock.writeLock().lock();
            this.wrapped.remove(listenerClass, listener);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public <L extends Listener> Stream<L> get(Class<L> listenerClass) {
        try {
            this.lock.readLock().lock();
            return this.wrapped.get(listenerClass);
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public <L extends Listener> void clearAll(Class<L> listenerClass) {
        try {
            this.lock.writeLock().lock();
            this.wrapped.clearAll(listenerClass);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public void clearAll() {
        try {
            this.lock.writeLock().lock();
            this.wrapped.clearAll();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isSequential() {
        return this.wrapped.isSequential();
    }

    @Override
    public void close() {
        try {
            this.lock.writeLock().lock();
            this.wrapped.close();
        } finally {
            this.lock.writeLock().unlock();
        }
    }

}
