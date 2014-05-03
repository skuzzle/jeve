package de.skuzzle.jeve;

/**
 * Interface for providing errors which occur during event dispatching to the caller.
 * 
 * @author Simon Taddiken
 * @since 1.0.0
 */
public interface ExceptionCallback {

    /**
     * Callback method which gets passed an exception. This method will be called by
     * an {@link EventProvider} if an instance of this interface has been set as
     * exception callback. This method is generally called within the same thread in
     * which the attempt to notify the listener has been made. 
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
