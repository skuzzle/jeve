package de.skuzzle.jeve;

import java.util.function.Predicate;

/**
 * Holds predicates for assertions about events.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public final class EventPredicates {

    private EventPredicates() {
        // hidden
    }

    /**
     * Creates a predicate that matches events for the given listener class.
     *
     * @param cls The listener class to check for.
     * @return The predicate.
     */
    public static Predicate<Event<?, ?>> withListenerClass(
            Class<? extends Listener> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("cls is null");
        }
        return event -> event.getListenerClass() == cls;
    }
}
