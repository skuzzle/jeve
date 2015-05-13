package de.skuzzle.jeve.invoke;


/**
 * Represents an event invocation which failed due to a runtime exception thrown
 * by the {@link #getListener() listener}.
 *
 * @author Simon Taddiken
 */
public interface FailedEventInvocation extends EventInvocation {

    /**
     * The exception that occurred during dispatching the {@link #getEvent()
     * event}.
     *
     * @return The exception.
     */
    public Exception getException();
}
