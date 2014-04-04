package de.skuzzle.jeve.util;

import java.util.EventListener;

import org.junit.Ignore;


/**
 * StringListeners are notified about {@link StringEvent StringEvents}.
 * 
 * @author Simon Taddiken
 */
@Ignore
public interface StringListener extends EventListener {

    /**
     * This method is notified about occurring {@link StringEvent StringEvents}.
     * @param e The occurred event.
     */
    public void onStringEvent(StringEvent e);
}
