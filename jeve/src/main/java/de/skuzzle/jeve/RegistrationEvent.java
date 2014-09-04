package de.skuzzle.jeve;

/**
 * RegistrationEvents are created when adding or removing a {@link Listener}
 * from an {@link EventProvider}.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
public final class RegistrationEvent {

    /** The {@link EventProvider} to which the listener has been added */
    private final EventProvider source;

    /** The class for which the listener has been added or removed */
    private final Class<? extends Listener> cls;

    /**
     * Creates a new RegistrationEvent.
     *
     * @param source The EventProvider for which a Listener as added or removed.
     * @param cls The class for which the Listener was added ore removed.
     */
    RegistrationEvent(EventProvider source, Class<? extends Listener> cls) {
        this.source = source;
        this.cls = cls;
    }

    /**
     * Gets the EventProvider to which the listener has been added.
     *
     * @return The EventProvider.
     */
    public EventProvider getSource() {
        return this.source;
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
