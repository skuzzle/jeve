package de.skuzzle.jeve.builder;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.AsyncProviderConfigurator;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Chainable;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Final;
import de.skuzzle.jeve.providers.ExecutorAware;
import de.skuzzle.jeve.providers.StatisticsEventProvider;

class AsyncProviderConfiguratorImpl<E extends EventProvider>
        implements AsyncProviderConfigurator<E> {

    private final Function<ListenerStore, E> providerConstructor;
    private final Supplier<? extends ListenerStore> storeSupplier;

    private Supplier<ExceptionCallback> ecSupplier;
    private Supplier<ExecutorService> executorSupplier;
    private boolean synchStore;

    AsyncProviderConfiguratorImpl(Function<ListenerStore, E> providerConstructor,
            Supplier<? extends ListenerStore> storeSupplier) {
        if (providerConstructor == null) {
            throw new IllegalArgumentException("providerSupplier is null");
        } else if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }

        this.providerConstructor = providerConstructor;
        this.storeSupplier = storeSupplier;
    }

    private E create() {
        final ListenerStore store = this.synchStore
                ? this.storeSupplier.get().synchronizedView()
                : this.storeSupplier.get();
        final E result = this.providerConstructor.apply(store);
        if (this.ecSupplier != null) {
            result.setExceptionCallback(this.ecSupplier.get());
        }
        if (this.executorSupplier != null) {
            if (result instanceof ExecutorAware) {
                final ExecutorAware ea = (ExecutorAware) result;
                ea.setExecutorService(this.executorSupplier.get());
            } else {
                throw new IllegalStateException(String.format(
                        "The configured EventProvider %s does not support setting "
                                + "an ExecutorService", result));
            }
        }
        return result;
    }

    @Override
    public Chainable<AsyncProviderConfigurator<E>, E> exceptionCallBack(
            ExceptionCallback ec) {
        this.ecSupplier = () -> ec;
        return new Chainable<AsyncProviderConfigurator<E>, E>() {

            @Override
            public AsyncProviderConfigurator<E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }

        };
    }

    @Override
    public Chainable<AsyncProviderConfigurator<E>, E> exceptionCallBack(
            Supplier<ExceptionCallback> callBackSupplier) {
        if (callBackSupplier == null) {
            throw new IllegalArgumentException("callBackSupplier is null");
        }
        this.ecSupplier = callBackSupplier;
        return new Chainable<AsyncProviderConfigurator<E>, E>() {

            @Override
            public AsyncProviderConfigurator<E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }

        };
    }

    @Override
    public Chainable<AsyncProviderConfigurator<E>, E> synchronizeStore() {
        this.synchStore = true;
        return new Chainable<AsyncProviderConfigurator<E>, E>() {

            @Override
            public AsyncProviderConfigurator<E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }
        };
    }

    @Override
    public Final<StatisticsEventProvider<E>> statistics() {
        final Function<ListenerStore, StatisticsEventProvider<E>> ctor = store -> {
            // XXX: passed store will be null here!
            final E provider = AsyncProviderConfiguratorImpl.this.create();
            return new StatisticsEventProvider<E>(provider);
        };

        return new Final<StatisticsEventProvider<E>>() {
            @Override
            public Supplier<StatisticsEventProvider<E>> createSupplier() {
                return this::create;
            }

            @Override
            public StatisticsEventProvider<E> create() {
                // XXX: store parameter is not needed here, because the store is
                // already created for the wrapped provider
                return ctor.apply(null);
            }

        };
    }

    @Override
    public Chainable<AsyncProviderConfigurator<E>, E> executor(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor is null");
        }
        return executor(() -> executor);
    }

    @Override
    public Chainable<AsyncProviderConfigurator<E>, E> executor(
            Supplier<ExecutorService> executorSupplier) {
        if (executorSupplier == null) {
            throw new IllegalArgumentException("executorSupplier is null");
        }
        this.executorSupplier = executorSupplier;
        return new Chainable<AsyncProviderConfigurator<E>, E>() {

            @Override
            public AsyncProviderConfigurator<E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }

        };
    }
}
