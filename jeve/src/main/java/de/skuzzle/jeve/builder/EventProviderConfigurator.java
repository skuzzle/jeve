package de.skuzzle.jeve.builder;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.providers.AWTEventProvider;
import de.skuzzle.jeve.providers.AsynchronousEventProvider;
import de.skuzzle.jeve.providers.ParallelEventProvider;
import de.skuzzle.jeve.providers.StatisticsEventProvider;
import de.skuzzle.jeve.providers.SynchronousEventProvider;
import de.skuzzle.jeve.stores.DefaultListenerStore;

public interface EventProviderConfigurator {

    interface ProviderChoser<S extends ListenerStore> {

        <C, E extends EventProvider<S>> And<C, E> customProvider(
                CustomConfigurator<S, C, E> configurator);

        And<ProviderConfigurator<S, SynchronousEventProvider<S>>, SynchronousEventProvider<S>>
            synchronousProvider();

        And<AsyncProviderConfigurator<S, AsynchronousEventProvider<S>>, AsynchronousEventProvider<S>>
                asynchronousProvider();

        And<AsyncProviderConfigurator<S, ParallelEventProvider<S>>, ParallelEventProvider<S>>
                parallelProvider();

        And<ProviderConfigurator<S, AWTEventProvider<S>>, AWTEventProvider<S>> waitingAWTEventProvider();

        And<ProviderConfigurator<S, AWTEventProvider<S>>, AWTEventProvider<S>> asynchronAWTEventProvider();
    }

    interface With<C> {
        C with();
    }

    interface And<C, E> {
        C and();

        default Supplier<E> asSupplier() {
            return () -> create();
        }

        E create();
    }

    interface ProviderConfigurator<S extends ListenerStore, E extends EventProvider<S>> {

        And<ProviderConfigurator<S, E>, E> exceptionCallBack(ExceptionCallback ec);

        And<ProviderConfigurator<S, E>, E> exceptionCallBack(
                Supplier<ExceptionCallback> callBackSupplier);

        And<ProviderConfigurator<S, StatisticsEventProvider<S, E>>, StatisticsEventProvider<S, E>> statistics();
    }

    interface AsyncProviderConfigurator<S extends ListenerStore, E extends EventProvider<S>> {

        And<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(ExceptionCallback ec);

        And<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(
                Supplier<ExceptionCallback> callBackSupplier);

        And<AsyncProviderConfigurator<S, E>, E> executor(ExecutorService executor);

        And<AsyncProviderConfigurator<S, E>, E> executor(
                Supplier<ExecutorService> executorSupplier);

        And<ProviderConfigurator<S, StatisticsEventProvider<S, E>>, StatisticsEventProvider<S, E>> statistics();
    }

    With<ProviderChoser<DefaultListenerStore>> defaultStore();

    <S extends ListenerStore> With<ProviderChoser<S>> store(
            Supplier<S> storeSupplier);

    <S extends ListenerStore> With<ProviderChoser<S>> store(S store);
}
