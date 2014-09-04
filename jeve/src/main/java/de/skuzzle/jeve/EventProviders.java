package de.skuzzle.jeve;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Utility class for obtaining different kinds of {@link EventProvider}
 * implementations.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public final class EventProviders {

    private EventProviders() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@link EventProvider} which fires events sequentially in
     * the thread which calls {@link EventProvider#dispatch(Event, BiConsumer)}.
     * The returned instance thus is sequential and supports aborting of event
     * delegation.
     *
     * <p>
     * Closing the {@link EventProvider} returned by this method will have no
     * effect besides removing all registered listeners.
     * </p>
     *
     * @return A new EventProvider instance.
     */
    public static EventProvider newDefaultEventProvider() {
        return new SynchronousEventProvider();
    }

    /**
     * Creates a new {@link EventProvider} which fires each event in a different
     * thread. By default, the returned {@link EventProvider} uses a single
     * thread executor service.
     *
     * <p>
     * The returned instance is sequential and supports aborting of event
     * delegation. Even when using multiple threads to dispatch events, the
     * returned EventProvider will only use one thread for one dispatch action.
     * That means that for each call to
     * {@link EventProvider#dispatch(Event, BiConsumer, ExceptionCallback)
     * dispatch}, all targeted listeners are notified within the same thread.
     * This ensures notification in the order the listeners have been added.
     * </p>
     *
     * <p>
     * When closing the returned {@link EventProvider}, its internal
     * {@link ExecutorService} instance will be shut down. Its not possible to
     * reuse the provider after closing it.
     * </p>
     *
     * @return A new EventProvider instance.
     */
    public static EventProvider newAsynchronousEventProvider() {
        return new AsynchronousEventProvider();
    }

    /**
     * Creates a new {@link EventProvider} which fires each event in a different
     * thread. The created provider will use the given {@link ExecutorService}
     * to fire the events asynchronously.
     *
     * <p>
     * The returned instance is sequential and supports aborting of event
     * delegation. Even when using multiple threads to dispatch events, the
     * returned EventProvider will only use one thread for one dispatch action.
     * That means that for each call to
     * {@link EventProvider#dispatch(Event, BiConsumer, ExceptionCallback)
     * dispatch}, all targeted listeners are notified within the same thread.
     * This ensures notification in the order the listeners have been added.
     * </p>
     *
     * <p>
     * If you require an EventListener which notifies each listener in a
     * different thread, use
     * {@link EventProviders#newParallelEventProvider(ExecutorService)}.
     * </p>
     *
     * <p>
     * When closing the returned {@link EventProvider}, the passed
     * {@link ExecutorService} instance will be shut down. Its not possible to
     * reuse the provider after closing it.
     * </p>
     *
     * @param executor The ExecutorService to use.
     * @return A new EventProvider instance.
     */
    public static EventProvider newAsynchronousEventProvider(ExecutorService executor) {
        return new AsynchronousEventProvider(executor);
    }

    /**
     * Create a new {@link EventProvider} which dispatches all events in the AWT
     * event thread and waits (blocks current thread) after dispatching until
     * all listeners have been notified. The returned instance is sequential and
     * supports aborting of event delegation.
     *
     * <p>
     * Closing the {@link EventProvider} returned by this method will have no
     * effect besides removing all registered listeners.
     * </p>
     *
     * @return A new EventProvider instance.
     */
    public static EventProvider newWaitingAWTEventProvider() {
        return new AWTEventProvider(true);
    }

    /**
     * Creates a new {@link EventProvider} which dispatches all events in the
     * AWT event thread. Dispatching with this EventProvider will return
     * immediately and dispatching of an event will be scheduled to be run later
     * by the AWT event thread. The returned instance is sequential and supports
     * aborting of event delegation.
     *
     * <p>
     * Closing the {@link EventProvider} returned by this method will have no
     * effect besides removing all registered listeners.
     * </p>
     *
     * @return A new EventProvider instance.
     */
    public static EventProvider newAsynchronousAWTEventProvider() {
        return new AWTEventProvider(false);
    }

    /**
     * Creates an EventProvider which notifies each listener within an own
     * thread. This means that for an single event, multiple threads might get
     * created to notify all listeners concurrently. The internal thread
     * creation is handled by an {@link Executors#newCachedThreadPool() cached
     * thread pool}. The returned EventProvider instance is not sequential and
     * does not support aborting of event delegation, as the correct order of
     * delegation can not be guaranteed.
     *
     * <p>
     * When closing the returned {@link EventProvider}, its internal
     * {@link ExecutorService} instance will be shut down. Its not possible to
     * reuse the provider after closing it.
     * </p>
     *
     * @return A new EventProvider instance.
     * @since 1.1.0
     */
    public static EventProvider newParallelEventProvider() {
        return new ParallelEventProvider(Executors.newCachedThreadPool());
    }

    /**
     * Creates an EventProvider which notifies each listener within an own
     * thread. This means that for an single event, multiple threads might get
     * created to notify all listeners concurrently. The internal thread
     * creation is handled by the passed {@link ExecutorService}. The returned
     * EventProvider instance is not sequential and does not support aborting of
     * event delegation, as the correct order of delegation can not be
     * guaranteed.
     *
     * <p>
     * When closing the returned {@link EventProvider}, the passed
     * {@link ExecutorService} instance will be shut down. Its not possible to
     * reuse the provider after closing it.
     * </p>
     *
     * @param executor The ExecutorService to use.
     * @return A new EventProvider instance.
     * @since 1.1.0
     */
    public static ParallelEventProvider newParallelEventProvider(
            ExecutorService executor) {
        return new ParallelEventProvider(executor);
    }

}
