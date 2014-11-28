package de.skuzzle.jeve;

import java.io.PrintStream;
import java.util.Collection;
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
 * @since 2.1.0
 */
public class EventStack {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventStack.class);

    /**
     * Contains the listener classes for which a dispatch action is currently
     * active.
     */
    private final LinkedList<Event<?, ?>> stack;

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

        dumpInternal(e -> out.printf("%s:%s:%s%n", e.getSource(),
                e.getListenerClass().getSimpleName(), e));
    }

    private void dumpInternal(Consumer<Event<?, ?>> c) {
        synchronized (this.stack) {
            LOGGER.debug("jeve event stack:");
            if (this.stack.isEmpty()) {
                LOGGER.debug("\t<empty>");
                return;
            }
            final Iterator<Event<?, ?>> it = this.stack.descendingIterator();
            it.forEachRemaining(c);
        }
    }

    /**
     * Pushes the event onto the event stack. This action must be performed
     * immediately before the event is being dispatched. Additionally, after the
     * event has been dispatched, it has to be {@link #popEvent(Event) popped}
     * off the stack again.
     *
     * @param <L> Type of the listener.
     * @param event the event which will be dispatched.
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
                    .anyMatch(cls -> c.contains(cls));
        }
    }

    public boolean isActive(Class<? extends Listener> listenerClass) {
        synchronized (this.stack) {
            return this.stack.stream()
                    .anyMatch(event -> event.getListenerClass() == listenerClass);
        }
    }


    public Optional<Event<?, ?>> preventDispatch(Event<?, ?> event) {
        return preventDispatch(event.getListenerClass());
    }

    public Optional<Event<?, ?>> preventDispatch(Class<? extends Listener> listenerClass) {
        synchronized (this.stack) {
            final Iterator<Event<?, ?>> it = this.stack.descendingIterator();
            while (it.hasNext()) {
                final Event<?, ?> event = it.next();
                if (event.getPrevented().contains(listenerClass)) {
                    return Optional.of(event);
                }
            }
            return Optional.empty();
        }
    }
}
