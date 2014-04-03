package de.skuzzle.jeve;

import java.util.EventListener;

/**
 * This is kind of a tagging interface for event listener which will only be
 * notified once. After being notified, the listener is removed from the 
 * {@link EventProvider} it was registered at.
 * 
 * <p>Usage of this interface is discouraged on asynchronous event providers which use
 * multiple threads, as the correct order of calling a listener method and the 
 * {@link #workDone(EventProvider)} method can not be guaranteed in general.</p>
 * 
 * @author Simon
 */
public interface OneTimeEventListener extends EventListener {
    
    /**
     * This method specifies whether this listner's work is done and it should be 
     * removed from its parent's {@link EventProvider} after the next time the listener
     * was notified.
     * @param parent The event provider from which the listener has been notified last.
     * @return Whether to remove this listener from its parent after next notification.
     */
    public boolean workDone(EventProvider parent);
}
