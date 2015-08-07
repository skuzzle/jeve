package de.skuzzle.jeve;

import java.util.stream.Stream;

/**
 * Supplies listeners to an {@link EventProvider}.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public interface ListenerSource {

    /**
     * Gets all listeners that should be notified for the given listenerClass.
     *
     * @param <L> Type of the listeners to return.
     * @param listenerClass The class representing the event for which the
     *            listeners should be retrieved.
     * @return A Stream of listeners that should be notified about the event
     *         represented by the given listener class.
     * @throws IllegalArgumentException If listenerClass is <code>null</code>.
     */
    <L extends Listener> Stream<L> get(Class<L> listenerClass);

}