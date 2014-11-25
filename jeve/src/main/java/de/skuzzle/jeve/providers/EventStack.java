package de.skuzzle.jeve.providers;

import java.util.Collection;
import java.util.Optional;
import java.util.Stack;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;

/**
 * Stack class which is used to keep track of currently dispatched {@link Event
 * Events}. Before an Event is being dispatched, it is pushed onto this stack
 * and right after it has been dispatched to all listeners it is popped off
 * again. This allows queries such as which events are currently active.
 *
 * @author Simon Taddiken
 * @since 2.1.0
 */
public class EventStack {

    /**
     * Contains the listener classes for which a dispatch action is currently
     * active.
     */
    private final Stack<Event<?, ?>> eventStack;

    /**
     * Creates a new EventStack.
     */
    public EventStack() {
        this.eventStack = new Stack<>();
    }

    /**
     * Pushes the event's listener class onto the event stack. This action must
     * be performed immediately before an event is being dispatched for that
     * listener class. Additionally, after the event has been dispatched, the
     * class has to be {@link #popEvent(Event) popped} off the stack.
     *
     *
     *
     * @param event the event which will be dispatched.
     * @see #popEvent(Event)
     */
    public <L extends Listener> void pushEvent(Event<?, L> event) {
        synchronized (this.eventStack) {
            this.eventStack.push(event);
        }
    }

    /**
     * Pops a listener class off the current event stack. This action has to be
     * performed immediately after an event has been dispatched to all
     * listeners.
     *
     * @param expected Event which provides the expected listener class.
     * @see #pushEvent(Event)
     */
    public <L extends Listener> void popEvent(Event<?, L> expected) {
        synchronized (this.eventStack) {
            final Event<?, ?> actual = this.eventStack.pop();
            if (actual != expected) {
                throw new IllegalStateException(String.format(
                        "Unbalanced pop: expected '%s' but encountered '%s'",
                        expected.getListenerClass(), actual));
            }
        }
    }

    /**
     * Determines whether an Event for the same listener class as the given
     * one's is currently being dispatched.
     *
     * @param event The event to retrieve the listener class from.
     * @return Whether an Event for the same listener class is already being
     *         dispatched.
     */
    public boolean isActive(Event<?, ?> event) {
        return isActive(event.getListenerClass());
    }

    /**
     * Determines whether an Event is being dispatched for at least one listener
     * class contained in the given collection.
     *
     * @param c Collection of listener classes.
     * @return Whether an Event for at least one class from c is already being
     *         dispatched.
     */
    public boolean isAnyActive(Collection<? extends Class<? extends Listener>> c) {
        synchronized (this.eventStack) {
            return this.eventStack.stream()
                    .anyMatch(cls -> c.contains(cls));
        }
    }

    public boolean isActive(Class<? extends Listener> listenerClass) {
        synchronized (this.eventStack) {
            return this.eventStack.stream()
                    .anyMatch(event -> event.getListenerClass() == listenerClass);
        }
    }

    public Optional<Event<?, ?>> preventDispatch(Event<?, ?> event) {
        return preventDispatch(event.getListenerClass());
    }

    public Optional<Event<?, ?>> preventDispatch(Class<? extends Listener> listenerClass) {
        synchronized (this.eventStack) {
            return this.eventStack.stream()
                    .filter(event -> event.getPrevented().contains(listenerClass))
                    .findAny();
        }
    }
}
