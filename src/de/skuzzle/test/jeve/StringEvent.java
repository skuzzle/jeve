package de.skuzzle.test.jeve;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;

/**
 * Event class which holds a single string.
 * 
 * @author Simon Taddiken
 */
public class StringEvent extends Event<EventProvider> {

    private final String string;
    
    /**
     * Creates a new StringEvent.
     * @param source The source of this event.
     * @param string The string attached to the event.
     */
    public StringEvent(EventProvider source, String string) {
        super(source);
        this.string = string;
    }

    
    
    /**
     * Gets the string attached to this event object.
     * @return The string.
     */
    public String getString() {
        return this.string;
    }
}
