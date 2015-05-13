package de.skuzzle.jeve.invoke;

import java.util.Objects;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.skuzzle.jeve.AbortionException;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;

class EventInvocationImpl<L extends Listener, E extends Event<?, L>>
        implements EventInvocation {

    private static final Logger LOG = LoggerFactory.getLogger(EventInvocation.class);

    protected final E event;
    protected final ExceptionCallback ec;
    protected final L listener;
    protected final BiConsumer<L, E> consumer;

    EventInvocationImpl(E event, L listener, ExceptionCallback ec,
            BiConsumer<L, E> consumer) {
        this.event = event;
        this.listener = listener;
        this.ec = ec;
        this.consumer = consumer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.event, this.listener);
    }

    @Override
    public boolean equals(Object obj) {
        final EventInvocation other;
        return obj == this || obj instanceof EventInvocation &&
            this.event.equals(((other = (EventInvocation) obj).getEvent())) &&
            this.listener.equals(other.getListener());
    }

    @Override
    public E getEvent() {
        return this.event;
    }

    @Override
    public L getListener() {
        return this.listener;
    }

    @Override
    public final void notifyListener() {
        try {
            this.consumer.accept(this.listener, this.event);
        } catch (AbortionException e) {
            throw e;
        } catch (RuntimeException e) {
            handleException(fail(e));
        }
    }

    @Override
    public FailedEventInvocation fail(Exception e) {
        return new FailedEventInvocationImpl<>(this.event, this.listener, this.ec,
                this.consumer, e);
    }

    private void handleException(FailedEventInvocation inv) {
        try {
            this.ec.exception(inv);
        } catch (AbortionException abort) {
            throw abort;
        } catch (Exception ignore) {
            LOG.error("ExceptionCallback '{}' threw an exception", this.ec, ignore);
            // where is your god now?
        }
    }

}