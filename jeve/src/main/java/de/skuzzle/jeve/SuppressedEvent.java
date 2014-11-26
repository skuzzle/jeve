package de.skuzzle.jeve;

import java.util.function.BiConsumer;

public class SuppressedEvent<L extends Listener, E extends Event<?, L>> {

    private final E event;
    private final ExceptionCallback ec;
    private final BiConsumer<L, E> consumer;
    private boolean dispatched;

    public SuppressedEvent(E event, ExceptionCallback ec, BiConsumer<L, E> consumer) {
        this.event = event;
        this.ec = ec;
        this.consumer = consumer;
    }

    public E getEvent() {
        return this.event;
    }

    public void redispatch(EventProvider<?> provider) {
        if (!this.dispatched) {
            this.dispatched = true;
            provider.dispatch(this.event, this.consumer, this.ec);
        }
    }
}
