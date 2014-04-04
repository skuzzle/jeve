package de.skuzzle.jeve.util;

import java.util.EventListener;

import org.junit.Ignore;

/**
 * DifferentStringListeners are notified about {@link StringEvent StringEvents}.
 * 
 * @author Simon Taddiken
 */
@Ignore
public interface DifferentStringListener extends EventListener {
    /**
     * This method is notified about occurring {@link StringEvent StringEvents}.
     * @param e The occurred event.
     */
    public void onDifferentStringEvent(StringEvent e);

}
