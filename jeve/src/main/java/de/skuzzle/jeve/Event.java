package de.skuzzle.jeve;

import java.util.Optional;

import de.skuzzle.jeve.providers.SynchronousEventProvider;

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
 * listeners for a dispatch action. Furthermore, events can have a default
 * target within a notified listener. This allows to use the
 * {@link EventProvider#dispatch(Event) dispatch overload} which only takes an
 * Event as parameter. Events have to override
 * {@link #defaultDispatch(EventProvider, ExceptionCallback)} to use this
 * functionality.
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

    /** The store from which this listener is currently being notified. */
    private ListenerStore store;

    /**
     * Whether this event was prevented the last time it was passed to any
     * dispatch method.
     *
     * @since 2.1.0
     */
    private boolean prevented;

    /**
     * The cause of this event if it was dispatched from within a listening
     * method.
     *
     * @since 2.1.0
     */
    private Optional<Event<?, ?>> cause;

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
        this.cause = Optional.empty();
    }

    /**
     * Creates a new Event with a given source and cause. This constructor might
     * be used when dispatching a new Event from within a listening method.
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     * @param cause The cause of this event. Can be <code>null</code>.
     */
    public Event(T source, Class<L> listenerClass, Event<?, ?> cause) {
        this(source, listenerClass, Optional.ofNullable(cause));
    }

    /**
     * Creates a new Event with a given source and cause. When this Event will
     * be dispatched with a {@link SynchronousEventProvider}, the cause of this
     * event can be determined by obtaining the peek of the provider's
     * {@link SynchronousEventProvider#getEventStack() event stack} even if this
     * event is not directly fired from within a listening method. Consider the
     * following event implementation:
     *
     * <pre>
     * public class WhatEverEvent extends Event&lt;Source, WhatEverListener&gt; {
     *     public WhatEverEvent(Source source, Optional&lt;Event&lt;?, ?&gt;&gt; cause) {
     *         super(source, WhatEverListener.class, cause);
     *     }
     * }
     * </pre>
     *
     * The event can then be instantiated like this:
     *
     * <pre>
     * EventStack stack = provider.getEventStack();
     * WhatEverEvent e = new WhatEverEvent(source, stack.peek());
     * </pre>
     *
     * @param source The source of this event.
     * @param listenerClass The type of the listener which can handle this
     *            event. This value must not be <code>null</code>.
     * @param cause The cause of this event.
     * @since 2.1.0
     */
    public Event(T source, Class<L> listenerClass, Optional<Event<?, ?>> cause) {
        this(source, listenerClass);
        if (cause == null) {
            throw new IllegalArgumentException("cause is null");
        }

        this.cause = cause;
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
        return this.cause;
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
     * <p>
     * Dispatches this event with the given EventProvider using the listener's
     * default listening method. For example, if {@code userAdded} is the only
     * listening method (or the default one among others), this method should be
     * implemented as follows:
     * </p>
     *
     * <pre>
     * public void defaultDispatch(EventProvider&lt;?&gt; eventProvider, ExceptionCallback ec) {
     *     eventProvider.dispatch(this, UserListener::userAdded, ec);
     * }
     * </pre>
     *
     * <p>
     * This method should not be called directly on an Event object. Instead,
     * pass the event to {@link EventProvider#dispatch(Event)} or
     * {@link EventProvider#dispatch(Event, ExceptionCallback)}.
     * </p>
     *
     * <p>
     * The default implementation throws an
     * {@link UnsupportedOperationException}
     * </p>
     *
     * @param eventProvider The EventProvider to use for dispatching.
     * @param ec The exception call back to use for this dispatch action.
     * @throws UnsupportedOperationException If this event does not support
     *             default dispatch.
     * @since 2.1.0
     */
    public void defaultDispatch(EventProvider<?> eventProvider, ExceptionCallback ec) {
        throw new UnsupportedOperationException(String.format(
                "Event %s does not specify a default dispatch target", this));
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
