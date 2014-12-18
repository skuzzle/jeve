package de.skuzzle.jeve.builder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.providers.AWTEventProvider;
import de.skuzzle.jeve.providers.AsynchronousEventProvider;
import de.skuzzle.jeve.providers.ParallelEventProvider;
import de.skuzzle.jeve.providers.StatisticsEventProvider;
import de.skuzzle.jeve.providers.SynchronousEventProvider;
import de.skuzzle.jeve.providers.UnrollingEventProvider;
import de.skuzzle.jeve.stores.DefaultListenerStore;

/**
 * This interface and its nested interfaces define the fluent builder API for
 * the {@link EventProvider#configure() configuring} EventProvider instances.
 * <em>Fluent API</em> means, that the methods of this interface and the way in
 * which their results can be chained together, form a kind of DSL (Domain
 * Specific Language). The entry point for the fluent API is the
 * {@link EventProvider#configure()} method, which returns an implementation of
 * this interface.
 *
 * <p>
 * <b>Note: </b> This interface and any of its nested interfaces, are not
 * intended to be implemented by clients. Instead, when you want to incorporate
 * the fluent builder API for your own EventProvider implementation, implement
 * {@link CustomConfigurator} and pass an instance to
 * {@link ProviderChooser#useCustomProvider(CustomConfigurator)}:
 * </p>
 *
 * <pre>
 * <code>
 * ... = EventProvider.configure()
 *         .defaultStore().with()
 *         .customProvider(myCustomConfigurator)
 *         .myFluentAPIMethod();
 * </code>
 * </pre>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public interface EventProviderConfigurator {

    /**
     * Creates a new configurator instance.
     *
     * @return The new configurator.
     * @since 3.0.0
     */
    public static EventProviderConfigurator create() {
        return new EventProviderConfiguratorImpl();
    }

    /**
     * Configures the type of EventProvider to use on the previously chosen
     * ListenerStore.
     *
     * @author Simon Taddiken
     * @param <S> The type of the {@link ListenerStore} configured in the first
     *            step.
     * @since 2.0.0
     */
    interface ProviderChooser<S extends ListenerStore> {

        /**
         * Entry point method for incorporating the fluent API to create custom
         * {@link EventProvider} instances. See the {@link CustomConfigurator}
         * for further information.
         *
         * @param <C> Type of the returned Fluent API object
         * @param <E> Type of the configured EventProvider.
         * @param configurator The custom fluent API entry point.
         * @return Fluent API object for further configuration.
         */
        <C, E extends EventProvider<S>> Final<C, E> useCustomProvider(
                CustomConfigurator<S, C, E> configurator);

        /**
         * Creates a new {@link UnrollingEventProvider}. This provider fires
         * event sequentially within the current thread but will not fire nested
         * events. Instead, nested events will be added to a queue which will be
         * processed once the current dispatch action is done.
         *
         * @return Fluent API object for further configuration.
         * @since 3.0.0
         */
        Final<ProviderConfigurator<S, UnrollingEventProvider<S>>,
                UnrollingEventProvider<S>> useUnrollingProvider();

        /**
         * Configures a synchronous EventProvider which will dispatch all events
         * from within the thread in which its {@code dispatch} method was
         * called. The configured provider will be sequential, if the previously
         * selected {@link ListenerStore} is sequential. Closing the returned
         * Provider will have no additional effects besides removing all of its
         * registered listeners.
         *
         * @return Fluent API object for further configuration.
         */
        Final<ProviderConfigurator<S, SynchronousEventProvider<S>>,
                SynchronousEventProvider<S>> useSynchronousProvider();

        /**
         * Configures an {@link EventProvider} which fires each event in a
         * different thread. By default, the configured {@link EventProvider}
         * uses a single thread executor service.
         *
         * <p>
         * The returned instance is sequential if the previously configured
         * {@link ListenerStore} is sequential. Even when using multiple threads
         * to dispatch events, the returned EventProvider will only use one
         * thread for one dispatch action. That means that for each call to
         * {@link EventProvider#dispatch(Event, BiConsumer, ExceptionCallback)
         * dispatch}, all targeted listeners are notified within the same
         * thread. This ensures notification in the order the listeners have
         * been added.
         * </p>
         *
         * <p>
         * When closing the returned {@link EventProvider}, its internal
         * {@link ExecutorService} instance will be shut down. Its not possible
         * to reuse the provider after closing it.
         * </p>
         *
         * @return Fluent API object for further configuration.
         */
        Final<AsyncProviderConfigurator<S, AsynchronousEventProvider<S>>,
                AsynchronousEventProvider<S>> useAsynchronousProvider();

        /**
         * Configures an {@link EventProvider} which notifies each listener
         * within an own thread. This means that for a single event, multiple
         * threads might get created to notify all listeners concurrently. The
         * internal thread creation is by default handled by an
         * {@link Executors#newCachedThreadPool() cached thread pool}. The
         * returned EventProvider instance is not sequential and does not
         * support aborting of event delegation, as the correct order of
         * delegation can not be guaranteed.
         *
         * <p>
         * When closing the returned {@link EventProvider}, its internal
         * {@link ExecutorService} instance will be shut down. Its not possible
         * to reuse the provider after closing it.
         * </p>
         *
         * @return Fluent API object for further configuration.
         */
        Final<AsyncProviderConfigurator<S, ParallelEventProvider<S>>,
                ParallelEventProvider<S>> useParallelProvider();

        /**
         * Configures an {@link EventProvider} which dispatches all events in
         * the AWT event thread and waits (blocks current thread) after
         * dispatching until all listeners have been notified. The returned
         * instance is sequential if the previously configured
         * {@link ListenerStore} is sequential.
         *
         * <p>
         * Closing the {@link EventProvider} returned by this method will have
         * no effect besides removing all registered listeners.
         * </p>
         *
         * @return Fluent API object for further configuration.
         */
        Final<ProviderConfigurator<S, AWTEventProvider<S>>, AWTEventProvider<S>>
                useWaitingAWTEventProvider();

        /**
         * Configures an {@link EventProvider} which dispatches all events in
         * the AWT event thread. Dispatching with this EventProvider will return
         * immediately and dispatching of an event will be scheduled to be run
         * later by the AWT event thread. The returned instance is sequential if
         * the previously configured {@link ListenerStore} is sequential.
         *
         * <p>
         * Closing the {@link EventProvider} returned by this method will have
         * no effect besides removing all registered listeners.
         * </p>
         *
         * @return A new EventProvider instance.
         */
        Final<ProviderConfigurator<S, AWTEventProvider<S>>, AWTEventProvider<S>>
                useAsynchronAWTEventProvider();
    }

    /**
     * Allows chaining of fluent API objects using the word 'and' and provides
     * methods to obtain the configured {@link EventProvider}.
     *
     * @param <C> The type of the chained fluent API object.
     * @param <E> The type of the EventProvider which is created by by this
     *            object.
     * @author Simon Taddiken
     * @since 2.0.0
     */
    interface Final<C, E> {
        /**
         * Returns the chained fluent API object
         *
         * @return The fluent API object.
         */
        C and();

        /**
         * Returns a {@link Supplier} which can be used to recreate instances of
         * the configured EventProvider.
         * <p>
         * The default implementation behaves as if
         * </p>
         *
         * <pre>
         * <code>return () -&gt; create()</code>
         * </pre>
         *
         * @return A supplier for creating EventProviders.
         */
        default Supplier<E> createSupplier() {
            return this::create;
        }

        /**
         * Returns an instance of the configured EventProvider. Multiple calls
         * will always create a new EventProvider object.
         *
         * @return The configured EventProvider instance.
         */
        E create();
    }

    /**
     * Provides configuration mostly for non-threaded EventProviders.
     *
     * @author Simon Taddiken
     * @param <S> The type of the {@link ListenerStore} configured in the first
     *            step.
     * @param <E> The type of the {@link EventProvider} configured in the second
     *            step.
     * @since 2.0.0
     */
    interface ProviderConfigurator<S extends ListenerStore, E extends EventProvider<S>> {

        /**
         * Configures the {@link ExceptionCallback} to use.
         *
         * @param ec The ExceptionCallback.
         * @return Fluent API object for further configuration.
         */
        Final<ProviderConfigurator<S, E>, E> exceptionCallBack(ExceptionCallback ec);

        /**
         * Instruct the provider to use the synchronized (thread-safe) version
         * of the configured store.
         *
         * @return Fluent API object for further configuration.
         * @see ListenerStore#synchronizedView()
         */
        Final<ProviderConfigurator<S, E>, E> synchronizeStore();

        /**
         * Configures the {@link ExceptionCallback} to use as a supplier.
         *
         * @param callBackSupplier Supplier which supplies the
         *            ExceptionCallback.
         * @return Fluent API object for further configuration.
         */
        Final<ProviderConfigurator<S, E>, E> exceptionCallBack(
                Supplier<ExceptionCallback> callBackSupplier);

        /**
         * Wraps the so far configured provider with a
         * {@link StatisticsEventProvider} which counts all dispatch actions.
         * The nested EventProvider can be accessed using the
         * {@link StatisticsEventProvider#getWrapped()} method.
         *
         * @return Fluent API object for further configuration.
         */
        Final<ProviderConfigurator<S, StatisticsEventProvider<S, E>>,
                StatisticsEventProvider<S, E>> statistics();
    }

    /**
     * Provides configuration for EventProviders which use an
     * {@link ExecutorService} for dispatching Events using multi threading.
     *
     * @author Simon Taddiken
     * @param <S> The type of the {@link ListenerStore} configured in the first
     *            step.
     * @param <E> The type of the {@link EventProvider} configured in the second
     *            step.
     * @since 2.0.0
     */
    interface AsyncProviderConfigurator<S extends ListenerStore, E extends EventProvider<S>> {
        /**
         * Configures the {@link ExceptionCallback} to use.
         *
         * @param ec The ExceptionCallback.
         * @return Fluent API object for further configuration.
         */
        Final<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(ExceptionCallback ec);

        /**
         * Configures the {@link ExceptionCallback} to use as a supplier.
         *
         * @param callBackSupplier Supplier which supplies the
         *            ExceptionCallback.
         * @return Fluent API object for further configuration.
         */
        Final<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(
                Supplier<ExceptionCallback> callBackSupplier);

        /**
         * Configures the {@link ExecutorService} to use.
         *
         * @param executor The ExecutorService
         * @return Fluent API object for further configuration.
         */
        Final<AsyncProviderConfigurator<S, E>, E> executor(ExecutorService executor);

        /**
         * Instruct the provider to use the synchronized (thread-safe) version
         * of the configured store.
         *
         * @return Fluent API object for further configuration.
         * @see ListenerStore#synchronizedView()
         */
        Final<AsyncProviderConfigurator<S, E>, E> synchronizeStore();

        /**
         * Configures the {@link ExecutorService} to use as a supplier.
         *
         * @param executorSupplier Supplier which supplies the ExecutorService.
         * @return Fluent API object for further configuration.
         */
        Final<AsyncProviderConfigurator<S, E>, E> executor(
                Supplier<ExecutorService> executorSupplier);

        /**
         * Wraps the so far configured provider with a
         * {@link StatisticsEventProvider} which counts all dispatch actions.
         * The nested EventProvider can be accessed using the
         * {@link StatisticsEventProvider#getWrapped()} method.
         *
         * @return Fluent API object for further configuration.
         */
        Final<ProviderConfigurator<S, StatisticsEventProvider<S, E>>,
                StatisticsEventProvider<S, E>> statistics();
    }

    /**
     * Create a synchronous EventProvider with a {@link DefaultListenerStore}
     * without options to further configure it.
     *
     * @return The EventProvider.
     */
    public SynchronousEventProvider<DefaultListenerStore> createInstantly();

    /**
     * Chooses a {@link DefaultListenerStore} to manage {@link Listener
     * Listeners} for the EventProvider instance to be created.
     *
     * @return Fluent API object for further configuration.
     */
    ProviderChooser<DefaultListenerStore> defaultStore();

    /**
     * Use the provided supplier to lazily create the {@link ListenerStore} to
     * use.
     *
     * @param <S> The type of the ListenerStore.
     * @param storeSupplier Supplier which supplies the ListenerStore.
     * @return Fluent API object for further configuration.
     */
    <S extends ListenerStore> ProviderChooser<S> store(
            Supplier<S> storeSupplier);

    /**
     * Chooses the provided {@code store} to manage {@link Listener Listeners}
     * for the EventProvider instance to be created.
     *
     * @param <S> The type of the ListenerStore.
     * @param store The ListenerStore to use.
     * @return Fluent API object for further configuration.
     */
    <S extends ListenerStore> ProviderChooser<S> store(S store);
}
