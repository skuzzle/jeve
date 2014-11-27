package de.skuzzle.jeve.providers;

import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.SuppressedEvent;

public class SuppressedEventImpl<L extends Listener, E extends Event<?, L>> implements
        SuppressedEvent {

    private final E event;
    private final ExceptionCallback ec;
    private final BiConsumer<L, E> consumer;
    private boolean dispatched;

    public SuppressedEventImpl(E event, ExceptionCallback ec, BiConsumer<L, E> consumer) {
        this.event = event;
        this.ec = ec;
        this.consumer = consumer;
    }

    @Override
    public Class<L> getListenerClass() {
        return this.event.getListenerClass();
    }

    @Override
    public E getEvent() {
        return this.event;
    }

    @Override
    public void redispatch(EventProvider<?> provider) {
        if (!this.dispatched) {
            this.dispatched = true;
            provider.dispatch(this.event, this.consumer, this.ec);
        }
    }
}
