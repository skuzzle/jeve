package de.skuzzle.jeve.providers;

import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;

public class SuppressedEvent<L extends Listener, E extends Event<?, L>> {

    private final E event;
    private final ExceptionCallback ec;
    private final BiConsumer<L, E> consumer;

    public SuppressedEvent(E event, ExceptionCallback ec, BiConsumer<L, E> consumer) {
        this.event = event;
        this.ec = ec;
        this.consumer = consumer;
    }

    public E getEvent() {
        return this.event;
    }

    public void redispatch(EventProvider<?> provider) {
        provider.dispatch(this.event, this.consumer, this.ec);
    }
}
