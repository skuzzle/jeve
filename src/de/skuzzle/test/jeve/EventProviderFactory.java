package de.skuzzle.test.jeve;

import de.skuzzle.jeve.EventProvider;

/**
 * Interface to create EventProvider instances.
 * 
 * @author Simon Taddiken
 */
public interface EventProviderFactory {

    /**
     * Creates an EventProvider instance.
     * @return The created EventProvider.
     */
    public EventProvider create();
}
