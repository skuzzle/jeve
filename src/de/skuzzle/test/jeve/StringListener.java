package de.skuzzle.test.jeve;

import java.util.EventListener;


/**
 * StringListeners are notified about {@link StringEvent StringEvents}.
 * 
 * @author Simon Taddiken
 */
public interface StringListener extends EventListener {

    /**
     * This method is notified about occurring {@link StringEvent StringEvents}.
     * @param e The occurred event.
     */
    public void onStringEvent(StringEvent e);
}
