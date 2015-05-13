package de.skuzzle.jeve.invoke;

import java.util.Objects;
import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;

final class FailedEventInvocationImpl<L extends Listener, E extends Event<?, L>> extends
        EventInvocationImpl<L, E> implements FailedEventInvocation {

    private final Exception e;

    FailedEventInvocationImpl(E event, L listener, ExceptionCallback ec,
            BiConsumer<L, E> consumer, Exception e) {
        super(event, listener, ec, consumer);
        this.e = e;
    }

    @Override
    public Exception getException() {
        return this.e;
    }

    @Override
    public FailedEventInvocation fail(Exception e) {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.event, this.listener, this.e);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) &&
                this.e.equals(((FailedEventInvocation) obj).getException());
    }
}
