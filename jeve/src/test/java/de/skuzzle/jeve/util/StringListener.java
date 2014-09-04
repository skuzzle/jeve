package de.skuzzle.jeve.util;

import org.junit.Ignore;

import de.skuzzle.jeve.Listener;

/**
 * StringListeners are notified about {@link StringEvent StringEvents}.
 *
 * @author Simon Taddiken
 */
@Ignore
public interface StringListener extends Listener {

    /**
     * This method is notified about occurring {@link StringEvent StringEvents}.
     *
     * @param e The occurred event.
     */
    public void onStringEvent(StringEvent e);
}
