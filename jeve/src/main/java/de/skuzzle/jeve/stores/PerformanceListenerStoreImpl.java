package de.skuzzle.jeve.stores;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;

class PerformanceListenerStoreImpl extends DefaultListenerStoreImpl implements
        PerformanceListenerStore {

    private static class SynchronizedStore extends
            AbstractSynchronizedListenerStore<PerformanceListenerStore> implements
            PerformanceListenerStore {

        private SynchronizedStore(PerformanceListenerStore wrapped) {
            super(wrapped);
        }

        @Override
        public PerformanceListenerStore synchronizedView() {
            return this;
        }

        @Override
        public boolean isAutoOptimize() {
            return this.wrapped.isAutoOptimize();
        }

        @Override
        public boolean isOptimized() {
            return read(this.wrapped::isAutoOptimize);
        }

        @Override
        public void optimizeGet() {
            modify(this.wrapped::optimizeGet);
        }

    }

    /** Whether {@link #optimizeGet()} has already been called. */
    protected boolean optimized;

    /** Auto {@link #optimizeGet()} on first call to {@link #get(Class)} */
    protected final boolean autoOptimize;

    private SynchronizedStore synchView;

    /**
     * Creates a new PerformanceListenerStore.
     */
    public PerformanceListenerStoreImpl() {
        this(false);
    }

    /**
     * Creates a PerformanceListenerStore with optionally enabling auto
     * optimize. When auto optimize is enabled, {@link #optimizeGet()} will
     * automatically be called the first time {@link #get(Class)} is called.
     *
     * @param pAutoOptimize Whether to enable auto optimize.
     */
    public PerformanceListenerStoreImpl(boolean pAutoOptimize) {
        this.autoOptimize = pAutoOptimize;
    }

    @Override
    public synchronized PerformanceListenerStore synchronizedView() {
        if (this.synchView == null) {
            this.synchView = new SynchronizedStore(this);
        }
        return this.synchView;
    }

    @Override
    public boolean isAutoOptimize() {
        return this.autoOptimize;
    }

    @Override
    public void optimizeGet() {
        if (this.optimized) {
            return;
        }
        this.optimized = true;
        for (final Entry<?, List<Object>> e : this.listenerMap.entrySet()) {
            final List<Object> cpy = new CopyOnWriteArrayList<>(e.getValue());
            e.setValue(cpy);
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

    @Override
    public boolean isOptimized() {
        return this.optimized;
    }

    @Override
    public <T extends Listener> Stream<T> get(Class<T> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass");
        }
        if (this.autoOptimize) {
            optimizeGet();
        }
        final List<Object> targets = this.listenerMap.getOrDefault(listenerClass,
                Collections.emptyList());

        if (this.optimized || targets.isEmpty()) {
            return targets.stream().map(listenerClass::cast);
        } else {
            final int sizeHint = targets.size();
            return copyList(listenerClass, targets.stream(), sizeHint).stream();
        }
    }
}
