package de.skuzzle.jeve.providers;

import java.util.concurrent.ExecutorService;

import de.skuzzle.jeve.EventProvider;

/**
 * Exposes a setter method for an {@link ExecutorService} to use by an
 * {@link EventProvider}.
 *
 * @author Simon Taddiken
 */
public interface ExecutorAware {

    /**
     * Sets the {@link ExecutorService} to use.
     *
     * @param executor The executor service.
     */
    public void setExecutorService(ExecutorService executor);
}
