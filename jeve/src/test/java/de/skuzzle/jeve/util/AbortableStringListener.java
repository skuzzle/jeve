package de.skuzzle.jeve.util;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;

/**
 * Sample listener for testing new kind of listening methods which return a boolean
 * value instead of void.
 * 
 * @author Simon Taddiken
 */
@ListenerInterface
public interface AbortableStringListener extends Listener {

    public int onStringEvent(StringEvent e);
}
