package de.skuzzle.jeve;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


/**
 * <p>
 * This class is the base of all events that can be fired. It holds the source
 * of the event and provides methods to stop delegation to further listeners if
 * this event has been handled by one listener. Events are meant to be short
 * living objects which are only used once - one Event instance for each call to
 * {@link EventProvider#dispatch(Event, java.util.function.BiConsumer) dispatch}
 * . Any different usage might result in undefined behavior, especially when
 * using the {@link #isHandled()} property. Also, to prevent memory leaks, Event
 * objects should never be stored over longer time.
 * </p>
 * <p>
 * Events explicitly belong to one kind of {@link Listener} implementation which
 * is able to handle it. The class of this listener is passed to the constructor
 * and queried by the {@link EventProvider} when collecting a list of targeted
 * listeners for a dispatch action.
 * </p>
 *
 * <p>
 * Events are used in conjunction with the {@link EventProvider} and its
 * {@link EventProvider#dispatch(Event, java.util.function.BiConsumer, ExceptionCallback)
 * dispatch} method. The dispatch method serves for notifying all registered
 * listeners with a certain event. The EventProvider will stop notifying further
 * listeners as soon as one listener sets this class' {@link #isHandled()} to
 * <code>true</code>.
 * </p>
 *
 * <h2>Note on multi-threading</h2>
 * <p>
 * Event objects are not thread safe! Some EventProviders dispatch events
 * asynchronously. If the same event instance is used within different threads,
 * avoid modifying properties of the Event. Those modifications will result in
 * undefined behavior.
 * </p>
 *
 * @param <T> Type of the source of this event.
 * @param <L> Type of the listener which can handle this event.
 * @author Simon Taddiken
 * @since 1.0.0
 * @version 2.0.0
 */
public class Event<T, L extends Listener> {

    /** The source of the event */
    private final T source;

    /** Whether this event has been marked as handled */
    private boolean isHandled;

    /** The class of the listener which can handle this event */
    private final Class<L> listenerClass;

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

    /** The store from which this listener is currently being notified. */
    private ListenerStore store;

    /**
     * Whether this event was prevented the last time it was passed to any
     * dispatch method.
     */
    private boolean prevented;

    /**
     * The cause of this event if it was dispatched from within a listening
     * method.
     */
    private Event<?, ?> cause;

    /**
     * Creates a new event with a given source.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     */
    public Event(T source, Class<L> listenerClass) {
        if (listenerClass == null) {
            throw new IllegalArgumentException("listenerClass is null");
        }

        this.source = source;
        this.listenerClass = listenerClass;
        this.isHandled = false;
    }

    /**
     * Gets the source of this event.
     *
     * @return The source of this event.
     */
    public T getSource() {
        return this.source;
    }

    /**
     * Gets the cause of this event. An event has a cause if it was dispatched
     * from within a listening method.
     *
     * @return The cause of this event if it exists.
     * @since 2.1.0
     */
    public Optional<Event<?, ?>> getCause() {
        return Optional.ofNullable(this.cause);
    }

    /**
     * Whether this event was prevented the last time it has been passed to any
     * dispatch method of an {@link EventProvider}.
     *
     * @return Whether this event has been prevented.
     */
    public boolean isPrevented() {
        return this.prevented;
    }

    /**
     * Called only by {@link EventProvider EventProvider's} dispatch method if
     * this event was prevented from being dispatched.
     *
     * @param prevented Whether the event was prevented.
     */
    public void setPrevented(boolean prevented) {
        this.prevented = prevented;
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
        preventCascade(this.listenerClass);
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

    /**
     * Gets the type of the listener which can handle this event.
     *
     * @return The listener's type.
     * @version 2.0.0
     */
    public Class<L> getListenerClass() {
        return this.listenerClass;
    }

    /**
     * Gets the {@link ListenerStore} from which the currently notified listener
     * has been retrieved. If this Event is not currently dispatched, an
     * exception will be thrown.
     *
     * @return The ListenerStore
     */
    protected ListenerStore getListenerStore() {
        if (this.store == null) {
            throw new IllegalStateException("Event is not currently dispatched");
        }
        return this.store;
    }

    /**
     * Removes the provided listener from the {@link ListenerStore} from which
     * it was supplied to the EventProvider which is currently dispatching this
     * event. Hence this method can only be called from within a listening
     * method while the event is being dispatched. Calling this method on an
     * Event instance which is not currently dispatched will raise an exception.
     *
     * <pre>
     * <code>
     * public class OneTimeUserListener extends UserListener {
     *     &#64;Override
     *     public void userAdded(UserEvent e) {
     *         // logic goes here
     *         // ...
     *
     *         // this listener should not be notified any more about this kind of
     *         // event.
     *         e.stopNotifying(this);
     *     }
     * }
     * </code>
     * </pre>
     *
     * Removing the listener will have no effect on the current dispatch action.
     * Even if you remove a different listener than {@code this}, it will be
     * notified anyway during this run, because the EventProvider collects the
     * Listeners before starting to dispatch the event.
     *
     * @param listener The listener to remove from the currently dispatching
     *            {@link EventProvider}
     * @since 2.0.0
     */
    public void stopNotifying(L listener) {
        this.getListenerStore().remove(this.getListenerClass(), listener);
    }

    /**
     * Sets the ListenerStore from which the currently dispatching EventProvider
     * retrieves its Listeners. The method will only set the store once. A
     * second call to this method on the same event instance has no effect. This
     * is to allow wrapping EventProviders so that the store set by the
     * outermost provider is not overridden by an inner (wrapped) provider.
     *
     * @param store The listener store.
     * @since 2.0.0
     */
    public void setListenerStore(ListenerStore store) {
        if (this.store == null) {
            this.store = store;
        }
    }

    /**
     * Gets whether this event was already handled. If this returns
     * <code>true</code>, no further listeners will be notified about this
     * event.
     *
     * @return Whether this event was handled.
     */
    public boolean isHandled() {
        return this.isHandled;
    }

    /**
     * Sets whether this event was already handled. If an event has been marked
     * as "handled", no further listeners will be notified about it.
     *
     * <p>
     * Note that setting an event to be handled might have unintended side
     * effects when using an {@link EventProvider} which is not
     * {@link EventProvider#isSequential() sequential}.
     * </p>
     *
     * @param isHandled Whether this event was handled.
     */
    public void setHandled(boolean isHandled) {
        this.isHandled = isHandled;
    }
}