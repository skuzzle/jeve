package de.skuzzle.jeve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronousEvent.class);

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
     * The cause of this event.
     */
    private Optional<Event<?, ?>> cause;

    /**
     * Creates a new event with a given source.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     */
    public SynchronousEvent(T source, Class<L> listenerClass) {
        super(source, listenerClass);
        this.cause = Optional.empty();
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
            this.cause = eventStack.peek();
        }
    }

    /**
     * Gets the cause of this event. An event has a cause attached if it was
     * dispatched while another event has been dispatched.
     *
     * @return The cause of this event if it exists.
     */
    public Optional<Event<?, ?>> getCause() {
        return this.cause;
    }

    /**
     * Gets the current event stack. A stack will only be present if the event
     * is being dispatched by an event provider which calls
     * {@link #setEventStack(EventStack)} before dispatching (like
     * {@link SynchronousEventProvider}).
     *
     * @return The current event stack if present.
     */
    public Optional<EventStack> getEventStack() {
        return Optional.ofNullable(this.eventStack);
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
     * <p>
     * Note: this method has to be called before this event is being dispatched.
     * If called during dispatch, it will has no effect.
     * </p>
     *
     * @param <E> Type of the listener class.
     * @param listenerClass The listener class to prevent being notified.
     * @see #preventCascade()
     */
    public <E extends Listener> void preventCascade(Class<E> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        } else if (this.eventStack != null) {
            // this event is currently being dispatched, so adding a prevented
            // class has no effect.
            LOGGER.warn("'preventCascade' has been called on {} for listener class {} "
                    + "while the event was being dispatched. prventCascade must be "
                    + "called before dispatching the event.",
                    this.getClass().getSimpleName(),
                    getListenerClass().getSimpleName());
            return;
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
     */
    public Set<Class<?>> getPrevented() {
        if (this.prevent == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(this.prevent);
    }
}
