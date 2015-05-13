package de.skuzzle.jeve.invoke;

import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;

/**
 * Represents the action of notifying a single listener about an event.
 *
 * @author Simon Taddiken
 * @since 3.0.0
 */
public interface EventInvocation {

    public static <L extends Listener, E extends Event<?, L>> EventInvocation of(
            L listener, E event, BiConsumer<L, E> method, ExceptionCallback ec) {
        return new EventInvocationImpl<>(event, listener, ec, method);
    }

    /**
     * Gets the event to notify the listener about.
     *
     * @return The event.
     */
    public Event<?, ?> getEvent();

    /**
     * Gets the listener which is notified.
     *
     * @return The listener.
     */
    public Listener getListener();

    /**
     * Notifies the wrapped listener about the wrapped event. This method
     * performs all the exception handling described in {@link EventProvider}
     * and {@link ExceptionCallback}.
     */
    public void notifyListener();

    public FailedEventInvocation toFailedInvocation(Exception e);
}
