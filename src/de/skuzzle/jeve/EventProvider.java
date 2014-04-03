package de.skuzzle.jeve;

import java.util.Collection;
import java.util.EventListener;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;


/**
 * EventProviders are used to fire swing-style events. Listeners can be registered and
 * removed for a certain event. Furthermore the strategy on how to actually dispatch
 * fired event is implementation dependent. You can obtain different implementations
 * using the static factory methods or you may extend {@link AbstractEventProvider} to
 * provide your own customized provider.
 * 
 * @author Simon
 */
public interface EventProvider extends AutoCloseable {

    /**
     * Creates a new {@link EventProvider} which fires events sequentially in the thread
     * which calls {@link EventProvider#dispatch(Class, Event, BiConsumer)}.
     * 
     * <p>Closing the {@link EventProvider} returned by this method will have no 
     * effect.</p>
     * 
     * @return A new EventProvider instance.
     */
    public static EventProvider newDefaultEventProvider() {
        return new SynchronousEventProvider();
    }
    
    
    
    /**
     * Creates a new {@link EventProvider} which fires each event in a different thread.
     * By default, the returned {@link EventProvider} uses a single thread executor 
     * service.
     * 
     * <p>When closing the returned {@link EventProvider}, its internal 
     * {@link ExecutorService} instance will be shut down. Its not possible to reuse the
     * provider after closing it.</p>
     * 
     * @return A new EventProvider instance.
     */
    public static EventProvider newAsynchronousEventProvider() {
        return new AsynchronousEventProvider();
    }
    
    
    
    /**
     * Creates a new {@link EventProvider} which fires each event in a different thread.
     * The created provider will use the given {@link ExecutorService} to fire the events
     * asynchronously.
     * 
     * <p>When closing the returned {@link EventProvider}, the passed 
     * {@link ExecutorService} instance will be shut down. Its not possible to reuse the
     * provider after closing it.</p>
     * 
     * @param dispatcher The ExecutorService to use.
     * @return A new EventProvider instance.
     */
    public static EventProvider newAsynchronousEventProvider(ExecutorService dispatcher) {
        return new AsynchronousEventProvider(dispatcher);
    }
    
    
    
    /**
     * Create a new {@link EventProvider} which dispatches all events in the AWT event 
     * thread and waits (blocks current thread) after dispatching until all listeners
     * have been notified.
     * 
     * <p>Closing the {@link EventProvider} returned by this method will have no 
     * effect.</p>
     * 
     * @return A new EventProvider instance.
     */
    public static EventProvider newWaitingAWTEventProvider() {
        return new AWTEventProvider(true);
    }
    
    
    
    /**
     * Creates a new {@link EventProvider} which dispatches all events in the AWT event
     * thread. Dispatching with this EventProvider will return immediately and dispatching
     * of an event will be scheduled to be run later by the AWT event thread.
     * 
     * <p>Closing the {@link EventProvider} returned by this method will have no 
     * effect.</p>
     * 
     * @return A new EventProvider instance.
     */
    public static EventProvider newAsynchronousAWTEventProvider() {
        return new AWTEventProvider(false);
    }
    
    
    
    
    /**
     * Adds a listener which will be notified for every event represented by the
     * given listener class.
     * 
     * @param listenerClass The class representing the event(s) to listen on.
     * @param listener The listener to add.
     * @throws NullPointerException If either listenerClass or listener is 
     *          <code>null</code>.
     */
    public <T extends EventListener> void addListener(Class<T> listenerClass, 
            T listener);
    
    /**
     * Removes a listener. It will only be removed for the specified listener class and
     * can thus still be registered with this event provider if it was added for
     * further listener classes. The listener will no longer receive events represented
     * by the given listener class.
     *  
     * @param listenerClass The class representing the event(s) for which the listener
     *          should be removed.
     * @param listener The listener to remove.
     */
    public <T extends EventListener> void removeListener(Class<T> listenerClass, 
            T listener);
    
    /**
     * Gets all listeners that have been registered using 
     * {@link #addListener(Class, EventListener)} for the given listener class.
     * 
     * @param listenerClass The class representing the event for which the listeners
     *          should be retrieved.
     * @return A collection of listeners that should be notified about the event 
     *          represented by the given listener class.
     * @throws NullPointerException If listenerClass is <code>null</code>.          
     */
    public <T extends EventListener> Collection<T> getListeners(Class<T> listenerClass);
    
    /**
     * Notifies all listeners of a certain kind about an occurred event. If this provider 
     * is not ready for dispatching as determined by {@link #canDispatch()}, this method 
     * returns immediately without doing anything. This method will stop notifying further
     * listeners if the passed event has been marked 'handled' using 
     * {@link Event#setHandled(boolean)}.
     * 
     * <p>If a notified listener implements {@link OneTimeEventListener} and its 
     * {@link OneTimeEventListener#workDone() workDone} method returns true, the listener
     * will be removed from this EventProvider.</p>
     * 
     *  
     * <p>Consider an <tt>UserListener</tt> interface:</p>
     * <pre>
     * public interface UserListener {
     *     public void userAdded(UserEvent e);
     *     
     *     public void userDeleted(UserEvent e);
     * }
     * </pre>
     * 
     * Notifying all registered UserListeners about an added user is as easy as calling
     * <pre>
     * eventProvider.dispatchEvent(UserListener.class, event, UserListener::userAdded)
     * </pre>
     * 
     * This method ignores exceptions thrown by notified listeners. If an exception 
     * occurs, its stacktrace will be printed and the next listener will be notified.
     * 
     * @param listenerClass The kind of listeners to notify.
     * @param event The occurred event which shall be passed to each listener.
     * @param bc Function to delegate the event to the specific callback method of the 
     *          listener.
     */
    public default <L extends EventListener, E extends Event<?>> void dispatch(
            Class<L> listenerClass, E event, BiConsumer<L, E> bc) {
        this.dispatch(listenerClass, event, bc, e -> e.printStackTrace());
    }
    
    /**
     * Notifies all listeners of a certain kind about an occurred event. If this provider 
     * is not ready for dispatching as determined by {@link #canDispatch()}, this method 
     * returns immediately without doing anything. This method will stop notifying further
     * listeners if the passed event has been marked 'handled' using 
     * {@link Event#setHandled(boolean)}.
     * 
     * <p>If a notified listener implements {@link OneTimeEventListener} and its 
     * {@link OneTimeEventListener#workDone() workDone} method returns true, the listener
     * will be removed from this EventProvider.</p>
     *  
     * <p>Consider an <tt>UserListener</tt> interface:</p>
     * <pre>
     * public interface UserListener {
     *     public void userAdded(UserEvent e);
     *     
     *     public void userDeleted(UserEvent e);
     * }
     * </pre>
     * 
     * Notifying all registered UserListeners about an added user is as easy as calling
     * <pre>
     * eventProvider.dispatchEvent(UserListener.class, event, UserListener::userAdded, e -> logger.error(e));
     * </pre>
     * 
     * The {@link ExceptionCallback} gets notified when any of the listeners throws an
     * unexpected exception. If the exception handler itself throws an exception, it will
     * be ignored.
     * 
     * @param listenerClass The kind of listeners to notify.
     * @param event The occurred event which shall be passed to each listener.
     * @param bc Function to delegate the event to the specific callback method of the 
     *          listener.
     * @param ec Callback to be notified when any of the listeners throws an exception.
     */
    public <L extends EventListener, E extends Event<?>> void dispatch(
            Class<L> listenerClass, E event, BiConsumer<L, E> bc, ExceptionCallback ec);
    
    /**
     * Gets whether this EventProvider is ready for dispatching.
     * 
     * @return Whether further events can be dispatched using 
     *          {@link #dispatchEvent(Dispatchable)}
     */
    public boolean canDispatch();
    
    /**
     * Closes this EventProvider. Depending on its implementation, it might not be 
     * able to dispatch further events after disposing. On some implementations closing
     * might have no effect.
     */
    public void dispose();
    
    @Override
    public default void close() {
        dispose();
    }
}