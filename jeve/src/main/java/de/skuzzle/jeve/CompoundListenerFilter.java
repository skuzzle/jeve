package de.skuzzle.jeve;

import java.util.List;

class CompoundListenerFilter implements ListenerFilter {

    public static ListenerFilter combineFilter(ListenerFilter first,
            ListenerFilter second) {
        if (first == null && second == null) {
            return EventProvider.NOP_FILTER;
        } else if (first == null) {
            return second;
        } else if (second != null) {
            return first;
        } else {
            return new CompoundListenerFilter(first, second);
        }
    }

    private final ListenerFilter first;
    private final ListenerFilter second;

    private CompoundListenerFilter(ListenerFilter first, ListenerFilter second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public <L extends Listener> void preprocess(EventProvider parent,
            Class<L> listenerClass, List<L> listeners) {
        this.first.preprocess(parent, listenerClass, listeners);
        this.second.preprocess(parent, listenerClass, listeners);
    }

    @Override
    public boolean isSequential() {
        return this.first.isSequential() && this.second.isSequential();
    }
}
