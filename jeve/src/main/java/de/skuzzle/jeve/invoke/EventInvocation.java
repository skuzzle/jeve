package de.skuzzle.jeve.invoke;

import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;

/**
 * Represents the action of notifying a single listener about an event. The
 * notification can be triggered with {@link #notifyListener()}. If the
 * notification fails because the listener throws an exception, this invocation
 * will be converted into a {@link FailedEventInvocation} and passed to
 * {@link ExceptionCallback#exception(FailedEventInvocation)}.
 *
 * @author Simon Taddiken
 * @since 3.0.0
 */
public interface EventInvocation {

    /**
     * Creates a new EventInvocation. This method also serves as default
     * {@link EventInvocationFactory} when used as a method reference.
     *
     * @param <L> The type of the listener.
     * @param <E> The type of the event.
     * @param listener The listener to notify.
     * @param event The event to pass to the listener.
     * @param method The method of the listener to call.
     * @param ec The exception handler.
     * @return A new EventInvocation for notifying the given listener with given
     *         event.
     */
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

    /**
     * Gets the {@link ExceptionCallback} to be notified when invocation fails.
     *
     * @return The callback.
     * @since 4.0.0
     */
    public ExceptionCallback getExceptionCallback();

    /**
     * Creates a new {@link FailedEventInvocation} holding the given exception.
     *
     * @param e The exception which occurred while notifying the listener.
     * @return A new FailedEventInvocation.
     */
    public FailedEventInvocation fail(Exception e);
}
