package de.skuzzle.jeve.util;

import de.skuzzle.jeve.Listener;

/**
 * Sample listener for testing new kind of listening methods which return a boolean
 * value instead of void.
 * 
 * @author Simon Taddiken
 */
public interface AbortableStringListener extends Listener {

    public boolean onStringEvent(StringEvent e);
}
