package de.skuzzle.jeve;

import java.util.EventListener;

/**
 * This is the base interface for event listeners. It specifies a single default method 
 * which can be used to automatically remove instances of this listener from a certain 
 * parent. When implementing this interface, you should add methods which take a
 * subclass of {@link Event} as a single parameter. Doing so enables the listener to
 * be used in conjunction with an {@link EventProvider}.
 * 
 * @author Simon Taddiken
 */
public interface Listener extends EventListener {

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
     */
    public default boolean workDone(EventProvider parent) {
        return false;
    }
}