package de.skuzzle.jeve;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Optional;

/**
 * Stack class which is used to keep track of currently dispatched {@link Event
 * Events}. Before an Event is being dispatched, it is pushed onto this stack
 * and right after it has been dispatched to all listeners it is popped off
 * again. This allows queries such as which events are currently active.
 *
 * <p>
 * Implementations must be thread safe.
 * </p>
 * @author Simon Taddiken
 */
public interface EventStack {

    /**
     * Prints the current event stack to the log using SLF4j log level
     * <em>debug</em>.
     */
    void dumpStack();

    /**
     * Prints the current event stack to the provided {@link PrintStream}.
     *
     * @param out The stream to print the log to.
     */
    void dumpStack(PrintStream out);

    /**
     * Returns the head element of the current stack if there is any.
     *
     * @return The top of the event stack.
     */
    Optional<Event<?, ?>> peek();

    /**
     * Determines whether an Event for the same listener class as the given
     * one's is currently being dispatched.
     *
     * @param event The event to retrieve the listener class from.
     * @return Whether an Event for the same listener class is already being
     *         dispatched.
     */
    boolean isActive(Event<?, ?> event);

    /**
     * Determines whether an Event is being dispatched for at least one listener
     * class contained in the given collection.
     *
     * @param c Collection of listener classes.
     * @return Whether an Event for at least one class from c is already being
     *         dispatched.
     */
    boolean isAnyActive(Collection<? extends Class<? extends Listener>> c);

    /**
     * Determines whether an Event is being dispatched for the given listener
     * class.
     *
     * @param listenerClass The listener class to check for.
     * @return Whether an Event for the given listener class is already being
     *         dispatched.
     */
    boolean isActive(Class<? extends Listener> listenerClass);

    /**
     * Checks whether dispatch of the given event should be prevented. This is
     * the case if there is at least one {@link SequentialEvent} currently
     * being dispatched, on which {@link SequentialEvent#preventCascade(Class)}
     * has been called with the given event's listener class.
     *
     * @param event The event to check whether it should be prevented.
     * @return If present, the optional holds the SequentialEvent which
     *         prevented the given one. If not present, the given event should
     *         not be prevented.
     * @see EventStackHelper#checkPrevent(EventStack, Event,
     *      java.util.function.BiConsumer, ExceptionCallback)
     */
    Optional<SequentialEvent<?, ?>> preventDispatch(Event<?, ?> event);

    /**
     * Checks whether dispatch for events with the given event should be
     * prevented. This is the case if there is at least one
     * {@link SequentialEvent} currently being dispatched, on which
     * {@link SequentialEvent#preventCascade(Class)} has been called with the
     * given listener class.
     *
     * @param listenerClass The listener class to check for.
     * @return If present, the optional holds the SequentialEvent which
     *         prevents dispatch of the given listener class. If not present,
     *         events should not be prevented for the given class.
     * @see EventStackHelper#checkPrevent(EventStack, Event,
     *      java.util.function.BiConsumer, ExceptionCallback)
     */
    Optional<SequentialEvent<?, ?>> preventDispatch(
            Class<? extends Listener> listenerClass);
}
