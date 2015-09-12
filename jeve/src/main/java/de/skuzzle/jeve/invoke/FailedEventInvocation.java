package de.skuzzle.jeve.invoke;

import org.slf4j.Logger;


/**
 * Represents an event invocation which failed due to a runtime exception thrown
 * by the {@link #getListener() listener}.
 *
 * @author Simon Taddiken
 */
public interface FailedEventInvocation extends EventInvocation {

    /**
     * Logs the failed invocation using the given logger by printing some default
     * information with the log level ERROR.
     *
     * @param logger The Logger to use.
     * @param invocation The failed invocation to log.
     * @since 4.0.0
     */
    public static void log(Logger logger, FailedEventInvocation invocation) {
        logger.error(
                "Listener threw an exception while being notified\n" +
                "Details\n" +
                "    Listener: {}\n" +
                "    Event: {}\n" +
                "    Message: {}\n" +
                "    Current Thread: {}\n" +
                "    Stacktrace:\n",
                invocation.getListener(),
                invocation.getEvent(),
                invocation.getException().getMessage(),
                Thread.currentThread().getName(),
                invocation.getException());
    }

    /**
     * The exception that occurred during dispatching the {@link #getEvent()
     * event}.
     *
     * @return The exception.
     */
    public Exception getException();
}
