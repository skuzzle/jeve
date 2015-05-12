package de.skuzzle.jeve.invoke;

import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;

final class EventInvocationImpl<L extends Listener, E extends Event<?, L>>
        extends AbstractEventInvocation<L, E> implements EventInvocation {

    EventInvocationImpl(E event, L listener, ExceptionCallback ec,
            BiConsumer<L, E> consumer) {
        super(event, listener, ec, consumer);
    }

    @Override
    public FailedEventInvocation toFailedInvocation(Exception e) {
        return new FailedEventInvocationImpl<>(this.event, this.listener, this.ec,
                this.consumer, e);
    }
}
