package de.skuzzle.jeve;

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
     * @param event The event which is currently being processed. This event might be 
     *          <code>null</code> iff this method was called by either 
     *          {@link EventProvider#addListener(Class, Listener)} or 
     *          {@link EventProvider#removeListener(Class, Listener)}.
     */
    public void exception(Exception e, Listener source, Event<?> event);
}
