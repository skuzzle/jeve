package de.skuzzle.test.jeve.util;

import org.junit.Ignore;

import de.skuzzle.jeve.EventProvider;

/**
 * Interface to create EventProvider instances.
 * 
 * @author Simon Taddiken
 */
@Ignore
public interface EventProviderFactory {

    /**
     * Creates an EventProvider instance.
     * @return The created EventProvider.
     */
    public EventProvider create();
}
