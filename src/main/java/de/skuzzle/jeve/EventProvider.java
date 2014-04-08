package de.skuzzle.jeve;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;


/**
 * <p>EventProviders are used to fire swing-style events. Listeners can be registered and
 * removed for a certain event. A class which implements multiple listener interfaces
 * can be registered for each listener type independently.</p>
 * 
 * <p>The strategy on how events are dispatched is implementation dependent and totally
 * transparent to client code which uses the EventProvider. You can obtain different 
 * implementations using the static factory methods or you may extend 
 * {@link AbstractEventProvider} to provide your own customized provider.</p>
 * 
 * <p>Unless stated otherwise, all EventProvider instances obtained from static factory
 * methods are thread-safe. Additionally, all provided default implementations guarantee 
 * the order of notification to be the same as the order in which listener had been 
 * registered, meaning they are {@link #isSequential() sequential}.</p>
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
     * <p>Even when using multiple threads to dispatch events, the returned EventProvider 
     * will only use one thread for one dispatch action. That means that for each call to
     * {@link #dispatch(Class, Event, BiConsumer, ExceptionCallback) dispatch}, all 
     * targeted listeners are notified within the same thread. This ensures notification
     * in the order the listeners have been added.</p>
     * 
     * <p>If you require an EventListener which notifies each listener in a different 
     * thread, you need to create your own sub class of {@link AbstractEventProvider}.</p>
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
    
    
    
    /** The default {@link ExceptionCallback} which simply prints the stack trace */
    public final static ExceptionCallback DEFAULT_HANDLER = 
            (e, l, ev) -> e.printStackTrace();
            
            
    
    /**
     * Adds a listener which will be notified for every event represented by the
     * given listener class. After registration, the listener's 
     * {@link Listener#onRegister(RegistrationEvent) onRegister} method gets called to
     * notify the listener about being added to a new parent. This method is not subject
     * to the dispatching strategy implemented by this {@link EventProvider} and is 
     * called from the current thread.
     * 
     * @param <T> Type of the listener to add. 
     * @param listenerClass The class representing the event(s) to listen on.
     * @param listener The listener to add.
     * @throws NullPointerException If either listenerClass or listener is 
     *          <code>null</code>.
     */
    public <T extends Listener> void addListener(Class<T> listenerClass, 
            T listener);
    
    /**
     * Removes a listener. It will only be removed for the specified listener class and
     * can thus still be registered with this event provider if it was added for
     * further listener classes. The listener will no longer receive events represented
     * by the given listener class. After removal, the listener's 
     * {@link Listener#onUnregister(RegistrationEvent) onUnregister} method gets called to
     * notify the listener about being removed from a parent. This method is not subject
     * to the dispatching strategy implemented by this {@link EventProvider} and is 
     * called from the current thread.
     * 
     * @param <T> Type of the listener to remove.
     * @param listenerClass The class representing the event(s) for which the listener
     *          should be removed.
     * @param listener The listener to remove.
     */
    public <T extends Listener> void removeListener(Class<T> listenerClass, 
            T listener);
    
    /**
     * Gets all listeners that have been registered using 
     * {@link #addListener(Class, Listener)} for the given listener class.
     * 
     * @param <T> Type of the listeners to return.
     * @param listenerClass The class representing the event for which the listeners
     *          should be retrieved.
     * @return A collection of listeners that should be notified about the event 
     *          represented by the given listener class.
     * @throws NullPointerException If listenerClass is <code>null</code>.          
     */
    public <T extends Listener> Collection<T> getListeners(Class<T> listenerClass);
    
    /**
     * Removes all listeners which have been registered for the provided listener class.
     * 
     * @param <T> Type of the listeners to remove.
     * @param listenerClass The class representing the event for which the listeners 
     *          should be removed
     */
    public <T extends Listener> void clearAllListeners(Class<T> listenerClass);
    
    /**
     * Removes all registered listeners from this EventProvider.
     */
    public void clearAllListeners();
    
    /**
     * Notifies all listeners of a certain kind about an occurred event. If this provider 
     * is not ready for dispatching as determined by {@link #canDispatch()}, this method 
     * returns immediately without doing anything. This method will stop notifying further
     * listeners if the passed event has been marked 'handled' using 
     * {@link Event#setHandled(boolean)}.
     * 
     * <p>If a notified listener's 
     * {@link Listener#workDone(EventProvider) workDone} method returns true, 
     * the listener will be removed from this EventProvider right after it has been 
     * notified.</p>
     * 
     * <p>Note: The behavior of whether the result of <tt>workDone</tt> is checked before
     * or after the listener has been notified might change in a future release.</p>
     * 
     *  
     * <p>Consider an <tt>UserListener</tt> interface:</p>
     * <pre>
     * public interface UserListener extends Listener {
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
     * <p>This method uses the global {@link ExceptionCallback} provided to 
     * {@link #setExceptionCallback(ExceptionCallback)} or an default instance if none
     * has been explicitly set.</p>
     * 
     * <p>Please note that neither parameter to this method must be null.</p>
     * 
     * @param <L> Type of the listeners which will be notified.
     * @param <E> Type of the event which will be passed to a listener.
     * @param listenerClass The kind of listeners to notify.
     * @param event The occurred event which shall be passed to each listener.
     * @param bc Function to delegate the event to the specific callback method of the 
     *          listener.
     */
    public <L extends Listener, E extends Event<?>> void dispatch(
            Class<L> listenerClass, E event, BiConsumer<L, E> bc);
    
    /**
     * Notifies all listeners of a certain kind about an occurred event. If this provider 
     * is not ready for dispatching as determined by {@link #canDispatch()}, this method 
     * returns immediately without doing anything. This method will stop notifying further
     * listeners if the passed event has been marked 'handled' using 
     * {@link Event#setHandled(boolean)}.
     * 
     * <p>If a notified listener's 
     * {@link Listener#workDone(EventProvider) workDone} method returns true, 
     * the listener will be removed from this EventProvider right after it has been 
     * notified.</p>
     * 
     * <p>Note: The behavior of whether the result of <tt>workDone</tt> is checked before
     * or after the listener has been notified might change in a future release.</p>
     *  
     * <p>Consider an <tt>UserListener</tt> interface:</p>
     * <pre>
     * public interface UserListener extends Listener {
     *     public void userAdded(UserEvent e);
     *     
     *     public void userDeleted(UserEvent e);
     * }
     * </pre>
     * 
     * Notifying all registered UserListeners about an added user is as easy as calling
     * <pre>
     * eventProvider.dispatchEvent(UserListener.class, event, UserListener::userAdded, 
     *      e -&gt; logger.error(e));
     * </pre>
     * 
     * <p>The {@link ExceptionCallback} gets notified when any of the listeners throws an
     * unexpected exception. If the exception handler itself throws an exception, it will
     * be ignored. The callback provided to this method takes precedence over the 
     * global callback provided by {@link #setExceptionCallback(ExceptionCallback)}.</p>
     * 
     * <p>Please note that neither parameter to this method must be null.</p>
     * 
     * @param <L> Type of the listeners which will be notified.
     * @param <E> Type of the event which will be passed to a listener.
     * @param listenerClass The kind of listeners to notify.
     * @param event The occurred event which shall be passed to each listener.
     * @param bc Function to delegate the event to the specific callback method of the 
     *          listener.
     * @param ec Callback to be notified when any of the listeners throws an exception.
     */
    public <L extends Listener, E extends Event<?>> void dispatch(
            Class<L> listenerClass, E event, BiConsumer<L, E> bc, ExceptionCallback ec);
    
    /**
     * Gets whether this EventProvider is ready for dispatching.
     * 
     * @return Whether further events can be dispatched using 
     *          {@link #dispatch(Class, Event, BiConsumer, ExceptionCallback) dispatch}
     */
    public boolean canDispatch();
    
    /**
     * Sets the default {@link ExceptionCallback} which will be notified about 
     * exceptions when dispatching events without explicitly specifying an 
     * ExceptionCallback. The ExceptionCallback which is installed by default simply 
     * prints the stack traces to the error console.
     * 
     * <p>You can reset the ExceptionCallback to the default handler by providing 
     * <code>null</code> as parameter.</p>
     * @param ec The ExceptionCallback for handling event handler exceptions, or 
     *          <code>null</code> to use the default behavior.
     */
    public void setExceptionCallback(ExceptionCallback ec);
    
    /**
     * Returns whether this EventProvider is sequential, which means it strictly 
     * notifies listeners in the order in which they were registered for a certain event.
     *  
     * @return Whether this instance is sequential.
     */
    public boolean isSequential();
    
    /**
     * Closes this EventProvider. Depending on its implementation, it might not be 
     * able to dispatch further events after disposing. On some implementations closing
     * might have no effect.
     */
    @Override
    public void close();
}