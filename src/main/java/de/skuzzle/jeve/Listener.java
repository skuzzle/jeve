package de.skuzzle.jeve;

import java.util.EventListener;

/**
 * This is the base interface for event listeners. It specifies a default method 
 * which can be used to automatically remove instances of this listener from a certain 
 * parent and further methods that are notified when the listener is registered or removed
 * to or from an {@link EventProvider}. 
 * 
 * <h2>Default listeners</h2>
 * Normally, you create an interface extending <tt>Listener</tt> and add some 
 * <em>listening methods</em>. By default, those methods must adhere to the signature:
 * 
 * <pre>public void &lt;listeningName&gt;(&lt;subclass of Event&gt; e);</pre>
 * 
 * This allows you to provide a method reference conforming to the 
 * {@link java.util.function.BiConsumer BiConsumer} functional interface to the 
 * {@link EventProvider#dispatch(Class, Event, java.util.function.BiConsumer) dispatch}
 * method of an EventProvider.
 * 
 * <pre>eventProvider.dispatch(MyListener.class, someEventInstance, MyListener::listeningMethod);</pre>
 * 
 * <h2>Abortable listeners</h2>
 * Sometimes it is helpful to be able to stop event delegation at a certain time. For this
 * purpose a second kind of <em>listening methods</em> exists. Those return a boolean
 * value indicating whether to continue event delegation. They must adhere to the 
 * signature:
 * 
 * <pre>public boolean &lt;listeningName&gt;(&lt;subclass of Event&gt; e);</pre>
 * 
 * This kind of listening methods can be notified about an Event using the overload of 
 * {@link EventProvider#dispatch(Class, Event, java.util.function.BiFunction) dispatch}
 * which takes a <tt>BiFunction</tt> returning a boolean as argument. Implementations of
 * such listening methods should use the defined constants {@link #CONTINUE} and 
 * {@link #ABORT} as return values.
 * 
 * @author Simon Taddiken
 * @since 1.0.0
 */
public interface Listener extends EventListener {
    
    /** 
     * Return value for listening methods indicating to continue event delegation with 
     * next listener.
     * @since 1.1.0
     */
    public final static boolean CONTINUE = true;

    /**
     * Return value for listening methods indicating to abort event delegation.
     * @since 1.1.0
     */
    public final static boolean ABORT = false;
    
    
    
    /**
     * This method specifies whether this listner's work is done and it should be 
     * removed from its parent's {@link EventProvider} after the next time the listener
     * was notified. If this method throws an unchecked exception, it will be covered
     * by the parent EventProvider's {@link ExceptionCallback} mechanism. 
     * 
     * <p>Note: currently, every listener is at least notified once before checking the
     * result of this method the first time. This might change in future releases.</p>
     * 
     * <p>Note: the default implementation always returns <code>false</code>, meaning 
     * that the listener never gets removed automatically.</p>
     * 
     * @param parent The event provider from which the listener would be removed.
     * @return Whether to remove this listener from its parent after next notification.
     * @deprecated Since 1.1.0 - use {@link Event#removeListener(Listener)} instead.
     */
    @Deprecated
    public default boolean workDone(EventProvider parent) {
        return false;
    }
    
    
    
    /**
     * This method is called right after this listener has been registered to a new 
     * {@link EventProvider}. If this method throws an unchecked exception, it will be
     * covered by the new EventProvider's {@link ExceptionCallback}.
     *  
     * <p>Note: The default implementation does nothing.</p>
     *  
     * @param e This event object holds the new parent EventProvider and the class for
     *          which this listener has been registered.
     */
    public default void onRegister(RegistrationEvent e) {
        // default: do nothing
    }
    
    
    
    /**
     * This method is called right after this listener has been removed from an 
     * {@link EventProvider}. If this method throws an unchecked exception, it will be
     * covered by the former EventProvider's {@link ExceptionCallback}.
     *
     * <p>Note: The default implementation does nothing.</p>
     * 
     * @param e This event object holds the former parent EventProvider and the class for
     *          which this listener has been unregistered.
     */
    public default void onUnregister(RegistrationEvent e) {
        // default: do nothing
    }
}