package de.skuzzle.jeve;

import java.util.Collections;
import java.util.HashSet;
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
 * @since 2.1.0
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
     * The EventStack of the Eventprovider which is currently dispatching this
     * event.
     */
    private EventStack eventStack;

    public SynchronousEvent(T source, Class<L> listenerClass, Event<?, ?> cause) {
        super(source, listenerClass, cause);
    }

    public SynchronousEvent(T source, Class<L> listenerClass) {
        super(source, listenerClass);
    }

    public void setEventStack(EventStack eventStack) {
        if (eventStack == null) {
            this.eventStack = eventStack;
        }
    }

    public EventStack getEventStack() {
        if (this.eventStack == null) {
            throw new IllegalStateException("Event is not currently dispatched");
        }
        return this.eventStack;
    }

    public void addSuppressedEvent(SuppressedEvent e) {
        if (e == null) {
            throw new IllegalArgumentException("e is null");
        } else if (this.suppressedEvents == null) {
            this.suppressedEvents = new HashSet<>();
        }
        this.suppressedEvents.add(e);
    }

    public Set<SuppressedEvent> getSuppressedEvents() {
        if (this.suppressedEvents == null) {
            return Collections.emptySet();
        }
        return this.suppressedEvents;
    }

    /**
     * Prevents to dispatch events to the given listener class while this event
     * is being dispatched. This is only supported when dispatching this event
     * with an EventProvider which supports the {@link EventStack}.
     *
     * @param <E> Type of the listener class.
     * @param listenerClass The listener class to prevent being notified.
     * @since 2.1.0
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
     * @since 2.1.0
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
     * @since 2.1.0
     */
    public Set<Class<?>> getPrevented() {
        if (this.prevent == null) {
            return Collections.emptySet();
        }
        return this.prevent;
    }
}
