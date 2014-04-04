package de.skuzzle.jeve;

import java.util.EventListener;

/**
 * This is kind of a tagging interface for event listener which will only be
 * notified once. After being notified, the listener is removed from the 
 * {@link EventProvider} it was registered at.
 * 
 * <p>Using this interface within a multi-threaded environment requires extra 
 * cautiousness and custom synchronization as the {@link #workDone(EventProvider)} method
 * might get called from two threads concurrently (implying handling of two different
 * events).</p>
 * 
 * @author Simon
 */
public interface OneTimeEventListener extends EventListener {
    
    /**
     * This method specifies whether this listner's work is done and it should be 
     * removed from its parent's {@link EventProvider} after the next time the listener
     * was notified.
     * 
     * <p>Note: currently, every listener is at least notified once before checking the
     * result of this method the first time. This might change in future releases.</p>
     * 
     * @param parent The event provider from which the listener would be removed.
     * @return Whether to remove this listener from its parent after next notification.
     */
    public boolean workDone(EventProvider parent);
}
