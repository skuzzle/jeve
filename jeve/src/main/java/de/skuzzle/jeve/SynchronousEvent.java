package de.skuzzle.jeve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.skuzzle.jeve.providers.SynchronousEventProvider;

/**
 * Specialized Event for use with {@link SynchronousEventProvider
 * SynchronousEventProviders}. It supports prevention of nested Events when
 * being dispatched.
 *
 * @author Simon Taddiken
 * @param <T> Type of the source of this event.
 * @param <L> Type of the listener which can handle this event.
 * @since 3.0.0
 */
public class SynchronousEvent<T, L extends Listener> extends Event<T, L> {

    /**
     * Collects listener classes for which cascade should be prevented. Will be
     * lazily initialized.
     */
    private Set<Class<?>> prevent;

    /**
     * Stores all events which were prevented due to the registered listener
     * classes. Will be lazily initialized.
     */
    private Set<SuppressedEvent> suppressedEvents;

    /**
     * The EventStack of the EventProvider which is currently dispatching this
     * event.
     */
    private EventStack eventStack;

    /**
     * Creates a new Event with a given source and cause. This constructor might
     * be used when dispatching a new Event from within a listening method.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     * @param cause The cause of this event. Can be <code>null</code>.
     */
    public SynchronousEvent(T source, Class<L> listenerClass, Event<?, ?> cause) {
        super(source, listenerClass, cause);
    }

    /**
     * Creates a new Event with a given source and cause. Delegates to the
     * respective super constructor.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     * @param cause The cause of this event.
     */
    public SynchronousEvent(T source, Class<L> listenerClass, Optional<Event<?, ?>> cause) {
        super(source, listenerClass, cause);
    }

    /**
     * Creates a new event with a given source.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     */
    public SynchronousEvent(T source, Class<L> listenerClass) {
        super(source, listenerClass);
    }

    /**
     * Sets the EventStack which is used while this event is dispatched. This
     * method will be called by the {@link SynchronousEventProvider} right
     * before dispatching this event. The stack is only set once, subsequent
     * calls will have no effect.
     *
     * @param eventStack The current event stack.
     */
    public void setEventStack(EventStack eventStack) {
        if (this.eventStack == null) {
            this.eventStack = eventStack;
        }
    }

    /**
     * Gets the current event stack. This method can only be called while this
     * event is being dispatched by a provider which calls
     * {@link #setEventStack(EventStack)} before dispatching. If the stack has
     * not been set, this method will throw an exception.
     *
     * @return The current event stack.
     * @throws IllegalStateException If no stack has been set.
     */
    public EventStack getEventStack() {
        if (this.eventStack == null) {
            throw new IllegalStateException("Event is not currently dispatched");
        }
        return this.eventStack;
    }

    /**
     * Adds a {@linkplain SuppressedEvent} to the set of suppressed events of
     * this Event. If a capable EventProvider determines that a certain event
     * has been prevented because its listener class has been registered on this
     * event using {@link #preventCascade(Class)}, then the prevented event is
     * registered here using this method.
     *
     * @param e The event to add.
     * @throws IllegalArgumentException If the event is <code>null</code>.
     */
    public void addSuppressedEvent(SuppressedEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("e is null");
        } else if (this.suppressedEvents == null) {
            this.suppressedEvents = new HashSet<>();
        }
        this.suppressedEvents.add(e);
    }

    /**
     * Gets a read-only set of the suppressed events that have been registered
     * at this event using {@link #addSuppressedEvent(SuppressedEvent)}.
     *
     * @return The suppressed events.
     */
    public Set<SuppressedEvent> getSuppressedEvents() {
        if (this.suppressedEvents == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(this.suppressedEvents);
    }

    /**
     * Prevents to dispatch events to the given listener class while this event
     * is being dispatched. This is only supported when dispatching this event
     * with an EventProvider which supports the {@link EventStack}.
     *
     * @param <E> Type of the listener class.
     * @param listenerClass The listener class to prevent being notified.
     * @since 3.0.0
     * @see #preventCascade()
     */
    public <E extends Listener> void preventCascade(Class<E> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        } else if (this.prevent == null) {
            this.prevent = new HashSet<>();
        }
        this.prevent.add(listenerClass);
    }

    /**
     * Prevents to dispatch events with the same listener class as this one as
     * long as this event is being dispatched. This is only supported when
     * dispatching this event with an EventProvider which supports the
     * {@link EventStack}.
     *
     * @since 3.0.0
     * @see #preventCascade(Class)
     */
    public void preventCascade() {
        preventCascade(getListenerClass());
    }

    /**
     * Gets the listener classes to which dispatching should be prevented while
     * this event is being dispatched.
     *
     * @return The listener classes marked to prevent.
     * @see #preventCascade(Class)
     * @since 3.0.0
     */
    public Set<Class<?>> getPrevented() {
        if (this.prevent == null) {
            return Collections.emptySet();
        }
        return this.prevent;
    }
}
