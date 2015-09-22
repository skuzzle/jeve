package de.skuzzle.jeve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.skuzzle.jeve.invoke.FailedEventInvocation;

/**
 * Contains some default implementations of {@link ExceptionCallback}.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public final class ExceptionCallbacks {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionCallbacks.class);

    private ExceptionCallbacks() {
        // hidden
    }

    /**
     * The default {@link ExceptionCallback} which simply logs any failure but
     * takes no further action.
     *
     * @return The callback.
     */
    public static ExceptionCallback ignore() {
        return new ExceptionCallback() {
            @Override
            public void exception(FailedEventInvocation invocation) {
                FailedEventInvocation.log(LOG, invocation);
            }
        };
    }

    /**
     * The callback will set the Event's {@link Event#isHandled() handled} flag
     * to <code>true</code> to gracefully stop the delegation process when an
     * exception occurs. Like {@link #ignore()}, the exception will be logged.
     *
     * @return The callback.
     */
    public static ExceptionCallback stopOnError() {
        return new ExceptionCallback() {
            @Override
            public void exception(FailedEventInvocation invocation) {
                FailedEventInvocation.log(LOG, invocation);
                invocation.getEvent().setHandled(true);
            }
        };
    }

    /**
     * The callback will convert the occurred exception into an
     * {@link AbortionException} and then throw it. This stops the delegation
     * process with delegating the exception to the dispatcher of the event.
     *
     * @return The callback.
     */
    public static ExceptionCallback failOnError() {
        return new ExceptionCallback() {
            @Override
            public void exception(FailedEventInvocation invocation) {
                throw new AbortionException(invocation.getException());
            }
        };
    }
}
