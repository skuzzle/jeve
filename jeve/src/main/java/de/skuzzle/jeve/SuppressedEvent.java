package de.skuzzle.jeve;

import de.skuzzle.jeve.providers.SynchronousEventProvider;

/**
 * Holds information about an event which has not been dispatched because it was
 * prevented by an other event. If an EventProvider encounters that an Event
 * should not be fired because of other events currently being dispatched, it
 * attaches an instance of this class to the event which prevented this one.
 *
 * <p>
 * Implementations must implement {@code equals} and {@code hashCode} based on
 * the object returned by {@link #getEvent()}. This ensures that no duplicated
 * {@linkplain SuppressedEvent SuppressedEvents} for the same {@linkplain Event}
 * are added to an event.
 * </p>
 *
 * @author Simon Taddiken
 * @since 3.0.0
 * @see SynchronousEventProvider
 */
public interface SuppressedEvent {

    /**
     * The Event object which has not been dispatched.
     *
     * @return The event.
     */
    public Event<?, ?> getEvent();

    /**
     * The listener class of the suppressed event.
     *
     * @return The listener class of the event returned by {@link #getEvent()}.
     */
    public Class<? extends Listener> getListenerClass();

    /**
     * Tries to dispatch this event again using the given provider. If this
     * event is still prevented, it won't be dispatched. As the event takes the
     * normal way through the dispatch mechanism, it will then be added again as
     * a suppressed event to event which is preventing it.
     *
     * @param provider The EventProvider to dispatch the event with.
     */
    public void redispatch(EventProvider<?> provider);
}
