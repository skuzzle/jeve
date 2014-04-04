package de.skuzzle.jeve;

import java.util.EventListener;

/**
 * Interface for providing errors which occur during event dispatching to the caller.
 * 
 * @author Simon Taddiken
 */
public interface ExceptionCallback {

    /**
     * Callback method which gets passed an exception.
     * 
     * <p>Note: If this method throws any unchecked exceptions, they will be swallowed
     * by the EventProvider during error handling.</p>
     * 
     * @param e The exception which occurred during event dispatching.
     * @param source The event listener which caused the exception.
     * @param event The event which is currently being processed.
     */
    public void exception(Exception e, EventListener source, Event<?> event);
}
