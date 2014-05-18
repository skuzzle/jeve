package de.skuzzle.jeve.util;

import org.junit.Ignore;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;

/**
 * DifferentStringListeners are notified about {@link StringEvent StringEvents}.
 * 
 * @author Simon Taddiken
 */
@Ignore
@ListenerInterface
public interface DifferentStringListener extends Listener {
    /**
     * This method is notified about occurring {@link StringEvent StringEvents}.
     * @param e The occurred event.
     */
    public void onDifferentStringEvent(StringEvent e);

}
