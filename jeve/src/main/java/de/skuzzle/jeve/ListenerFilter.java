package de.skuzzle.jeve;

import java.util.List;

/**
 * Specifies a method to pre-process a list of Listeners right before they are
 * notified about an event. The {@link #preprocess(EventProvider, Class, List)}
 * method will be called by the {@link EventProvider} after collecting the
 * targeted Listeners for a certain event. Its result represents the Listeners
 * to actually notify.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public interface ListenerFilter {

    /**
     * Modifies the provided list of {@code listeners}.
     *
     * @param parent The provider which called this method.
     * @param listenerClass The class for which the listeners were registered.
     * @param listeners The listeners.
     */
    public <L extends Listener> void preprocess(EventProvider parent,
            Class<L> listenerClass, List<L> listeners);

    /**
     * Whether the modifications made by
     * {@link #preprocess(EventProvider, Class, List)} retain the original
     * ordering of the passed in list.
     *
     * @return Whether this filter retains the original ordering of listeners.
     */
    public boolean isSequential();
}
