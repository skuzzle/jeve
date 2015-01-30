package de.skuzzle.jeve;

/**
 * RegistrationEvents are created when adding or removing a {@link Listener}
 * to/from a {@link ListenerStore}.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
public final class RegistrationEvent extends Event<ListenerStore, Listener> {

    /** The class for which the listener has been added or removed */
    private final Class<? extends Listener> cls;

    /**
     * Creates a new RegistrationEvent.
     *
     * @param source The ListenerStore for which a Listener as added or removed.
     * @param cls The class for which the Listener was added ore removed.
     */
    public RegistrationEvent(ListenerStore source, Class<? extends Listener> cls) {
        super(source, Listener.class);
        this.cls = cls;
    }

    /**
     * Gets the class for which the Listener has been added or removed.
     *
     * @return The event class.
     */
    public Class<? extends Listener> getEventClass() {
        return this.cls;
    }
}
