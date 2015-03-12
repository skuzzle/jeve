package de.skuzzle.jeve;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stack class which is used to keep track of currently dispatched {@link Event
 * Events}. Before an Event is being dispatched, it is pushed onto this stack
 * and right after it has been dispatched to all listeners it is popped off
 * again. This allows queries such as which events are currently active.
 *
 * <p>
 * This class is thread safe.
 * </p>
 *
 * @author Simon Taddiken
 * @since 3.0.0
 */
public class EventStack {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventStack.class);

    /**
     * Contains the listener classes for which a dispatch action is currently
     * active.
     */
    private final Deque<Event<?, ?>> stack;

    /**
     * Creates a new EventStack.
     */
    public EventStack() {
        this.stack = new LinkedList<>();
    }

    /**
     * Prints the current event stack to the log using SLF4j log level
     * <em>debug</em>.
     */
    public void dumpStack() {
        LOGGER.debug("jeve event stack:");
        if (this.stack.isEmpty()) {
            LOGGER.debug("\t<empty>");
            return;
        }

        dumpInternal(e -> LOGGER.debug("\t{}:{}:{}", e.getSource(),
                e.getListenerClass().getSimpleName(), e));
    }

    /**
     * Prints the current event stack to the provided {@link PrintStream}.
     *
     * @param out The stream to print the log to.
     */
    public void dumpStack(PrintStream out) {
        if (out == null) {
            throw new IllegalArgumentException("out is null");
        }
        out.println("jeve event stack:");
        if (this.stack.isEmpty()) {
            out.println("\t<empty>");
            return;
        }
        dumpInternal(e -> out.printf("\t%s:%s:%s%n", e.getSource(),
                e.getListenerClass().getSimpleName(), e));
    }

    private void dumpInternal(Consumer<Event<?, ?>> c) {
        synchronized (this.stack) {
            final Iterator<Event<?, ?>> it = this.stack.iterator();
            it.forEachRemaining(c);
        }
    }

    /**
     * Returns the head element of the current stack if there is any.
     *
     * @return The top of the event stack.
     */
    public Optional<Event<?, ?>> peek() {
        return this.stack.isEmpty()
                ? Optional.empty()
                : Optional.of(this.stack.peek());
    }

    /**
     * Pushes the event onto the event stack. This action must be performed
     * immediately before the event is being dispatched. Additionally, after the
     * event has been dispatched, it has to be {@link #popEvent(Event) popped}
     * off the stack again.
     *
     * @param <L> Type of the listener.
     * @param event The event which will be dispatched.
     * @see #popEvent(Event)
     */
    public <L extends Listener> void pushEvent(Event<?, L> event) {
        synchronized (this.stack) {
            this.stack.push(event);
        }
    }

    /**
     * Pops the top event off the current event stack. This action has to be
     * performed immediately after the event has been dispatched to all
     * listeners.
     *
     * @param <L> Type of the listener.
     * @param expected The Event which is expected at the top of the stack.
     * @see #pushEvent(Event)
     */
    public <L extends Listener> void popEvent(Event<?, L> expected) {
        synchronized (this.stack) {
            final Event<?, ?> actual = this.stack.pop();
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
        synchronized (this.stack) {
            return this.stack.stream()
                    .anyMatch(event -> c.contains(event.getListenerClass()));
        }
    }

    /**
     * Determines whether an Event is being dispatched for the given listener
     * class.
     *
     * @param listenerClass The listener class to check for.
     * @return Whether an Event for the given listener class is already being
     *         dispatched.
     */
    public boolean isActive(Class<? extends Listener> listenerClass) {
        synchronized (this.stack) {
            return this.stack.stream()
                    .anyMatch(event -> event.getListenerClass() == listenerClass);
        }
    }

    /**
     * Checks whether dispatch of the given event should be prevented. This is
     * the case if there is at least one {@link SynchronousEvent} currently
     * being dispatched, on which {@link SynchronousEvent#preventCascade(Class)}
     * has been called with the given event's listener class.
     *
     * @param event The event to check whether it should be prevented.
     * @return If present, the optional holds the SynchronousEvent which
     *         prevented the given one. If not present, the given event should
     *         not be prevented.
     * @see EventStackHelper#checkPrevent(EventStack, Event,
     *      java.util.function.BiConsumer, ExceptionCallback)
     */
    public Optional<SynchronousEvent<?, ?>> preventDispatch(Event<?, ?> event) {
        return preventDispatch(event.getListenerClass());
    }

    /**
     * Checks whether dispatch for events with the given event should be
     * prevented. This is the case if there is at least one
     * {@link SynchronousEvent} currently being dispatched, on which
     * {@link SynchronousEvent#preventCascade(Class)} has been called with the
     * given listener class.
     *
     * @param listenerClass The listener class to check for.
     * @return If present, the optional holds the SynchronousEvent which
     *         prevents dispatch of the given listener class. If not present,
     *         events should not be prevented for the given class.
     * @see EventStackHelper#checkPrevent(EventStack, Event,
     *      java.util.function.BiConsumer, ExceptionCallback)
     */
    public Optional<SynchronousEvent<?, ?>> preventDispatch(
            Class<? extends Listener> listenerClass) {
        synchronized (this.stack) {
            final Iterator<Event<?, ?>> it = this.stack.descendingIterator();
            while (it.hasNext()) {
                final Event<?, ?> event = it.next();
                if (event instanceof SynchronousEvent<?, ?>) {
                    SynchronousEvent<?, ?> synchEvent = (SynchronousEvent<?, ?>) event;
                    if (synchEvent.getPrevented().contains(listenerClass)) {
                        return Optional.of(synchEvent);
                    }
                }
            }
            return Optional.empty();
        }
    }
}
