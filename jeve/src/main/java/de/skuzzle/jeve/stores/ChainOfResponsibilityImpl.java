package de.skuzzle.jeve.stores;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

final class ChainOfResponsibilityImpl implements ChainOfResponsibility {

    private final ListenerSource allListeners;
    private final LinkedHashSet<Class<? extends Listener>> chain;
    private final boolean filtered;
    private SynchronizedSource synchView;

    ChainOfResponsibilityImpl(
            boolean filtered,
            ListenerSource allListeners,
            LinkedHashSet<Class<? extends Listener>> chain) {
        this.filtered = filtered;
        this.allListeners = allListeners;
        this.chain = chain;
    }

    /**
     * This comparator orders listener by their occurrence index within the
     * chain. All listeners that do not participate in the chain are ordered to
     * the end.
     *
     * @author Simon Taddiken
     */
    private class ByIndexComparator implements Comparator<Listener> {

        @Override
        public int compare(Listener o1, Listener o2) {
            final int o1Index = indexOf(o1.getClass());
            final int o2Index = indexOf(o2.getClass());
            return Integer.compare(o1Index, o2Index);
        }

        private int indexOf(Class<?> cls) {
            final Iterator<Class<? extends Listener>> it =
                    ChainOfResponsibilityImpl.this.chain.iterator();
            int i = 0;
            while (it.hasNext()) {
                if (cls.equals(it.next())) {
                    return i;
                }
                ++i;
            }
            return Integer.MAX_VALUE;
        }
    }

    private final static class SynchronizedSource
            extends AbstractSynchronizedListenerSource<ChainOfResponsibility>
            implements ChainOfResponsibility {

        protected SynchronizedSource(ChainOfResponsibility wrapped) {
            super(wrapped);
        }

        @Override
        public boolean isSequential() {
            return this.wrapped.isSequential();
        }

        @Override
        public ChainOfResponsibility synchronizedView() {
            return this;
        }
    }

    static final class ChainBuilderImpl implements ChainBuilder {
        private final ListenerSource source;
        private final LinkedHashSet<Class<? extends Listener>> chain;
        private boolean filter;

        ChainBuilderImpl(ListenerSource source) {
            if (source == null) {
                throw new IllegalArgumentException("source is null");
            }
            this.source = source;
            this.chain = new LinkedHashSet<>();
        }

        @Override
        public ChainBuilder enabledFiltering() {
            this.filter = true;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ChainBuilder withParticipants(Class<? extends Listener>... listeners) {
            this.chain.addAll(Arrays.asList(listeners));
            return this;
        }

        @Override
        public ChainBuilder withParticipant(Class<? extends Listener> listener) {
            this.chain.add(listener);
            return this;
        }

        @Override
        public ListenerSource create() {
            return new ChainOfResponsibilityImpl(this.filter, this.source, this.chain);
        }
    }

    @Override
    public synchronized ChainOfResponsibility synchronizedView() {
        if (this.synchView == null) {
            this.synchView = new SynchronizedSource(this);
        }
        return this.synchView;
    }

    @Override
    public <L extends Listener> Stream<L> get(Class<L> listenerClass) {
        Stream<L> result = this.allListeners.get(listenerClass);
        if (this.filtered) {
            result = result.filter(listener -> this.chain.contains(listener.getClass()));
        }
        result = result.sorted(new ByIndexComparator());
        return result;
    }

    @Override
    public boolean isSequential() {
        return false;
    }

    @Override
    public void close() {
        this.chain.clear();
    }
}
