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

class AsyncProviderConfiguratorImpl<S extends ListenerStore, E extends EventProvider<S>>
        implements AsyncProviderConfigurator<S, E> {

    private final Function<S, E> providerConstructor;
    private final Supplier<S> storeSupplier;

    private Supplier<ExceptionCallback> ecSupplier;
    private Supplier<ExecutorService> executorSupplier;
    private boolean synchronizeStore;

    AsyncProviderConfiguratorImpl(Function<S, E> providerConstructor,
            Supplier<S> storeSupplier) {
        if (providerConstructor == null) {
            throw new IllegalArgumentException("providerSupplier is null");
        } else if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }

        this.providerConstructor = providerConstructor;
        this.storeSupplier = storeSupplier;
    }

    @SuppressWarnings("unchecked")
    private E create() {
        final S store = this.synchronizeStore
                ? (S) this.storeSupplier.get().synchronizedView()
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
    public Chainable<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(
            ExceptionCallback ec) {
        this.ecSupplier = () -> ec;
        return new Chainable<AsyncProviderConfigurator<S, E>, E>() {

            @Override
            public AsyncProviderConfigurator<S, E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }

        };
    }

    @Override
    public Chainable<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(
            Supplier<ExceptionCallback> callBackSupplier) {
        if (callBackSupplier == null) {
            throw new IllegalArgumentException("callBackSupplier is null");
        }
        this.ecSupplier = callBackSupplier;
        return new Chainable<AsyncProviderConfigurator<S, E>, E>() {

            @Override
            public AsyncProviderConfigurator<S, E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }

        };
    }

    @Override
    public Chainable<AsyncProviderConfigurator<S, E>, E> synchronizeStore() {
        this.synchronizeStore = true;
        return new Chainable<AsyncProviderConfigurator<S, E>, E>() {

            @Override
            public AsyncProviderConfigurator<S, E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }
        };
    }

    @Override
    public Final<StatisticsEventProvider<S, E>> statistics() {
        final Function<S, StatisticsEventProvider<S, E>> ctor = store -> {
            // XXX: passed store will be null here!
            final E provider = AsyncProviderConfiguratorImpl.this.create();
            return new StatisticsEventProvider<S, E>(provider);
        };

        return new Final<StatisticsEventProvider<S, E>>() {
            @Override
            public Supplier<StatisticsEventProvider<S, E>> createSupplier() {
                return this::create;
            }

            @Override
            public StatisticsEventProvider<S, E> create() {
                // XXX: store parameter is not needed here, because the store is
                // already created for the wrapped provider
                return ctor.apply(null);
            }

        };
    }

    @Override
    public Chainable<AsyncProviderConfigurator<S, E>, E> executor(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor is null");
        }
        return executor(() -> executor);
    }

    @Override
    public Chainable<AsyncProviderConfigurator<S, E>, E> executor(
            Supplier<ExecutorService> executorSupplier) {
        if (executorSupplier == null) {
            throw new IllegalArgumentException("executorSupplier is null");
        }
        this.executorSupplier = executorSupplier;
        return new Chainable<AsyncProviderConfigurator<S, E>, E>() {

            @Override
            public AsyncProviderConfigurator<S, E> and() {
                return AsyncProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return AsyncProviderConfiguratorImpl.this.create();
            }

        };
    }
}
