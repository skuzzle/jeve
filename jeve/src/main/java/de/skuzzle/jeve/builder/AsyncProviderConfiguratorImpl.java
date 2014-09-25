package de.skuzzle.jeve.builder;

import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.AsyncProviderConfigurator;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Final;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderConfigurator;
import de.skuzzle.jeve.providers.ExecutorAware;
import de.skuzzle.jeve.providers.StatisticsEventProvider;

class AsyncProviderConfiguratorImpl<S extends ListenerStore, E extends EventProvider<S>>
        implements AsyncProviderConfigurator<S, E> {

    private final Supplier<E> providerSupplier;

    private Supplier<ExceptionCallback> ecSupplier;
    private Supplier<ExecutorService> executorSupplier;

    AsyncProviderConfiguratorImpl(Supplier<E> providerSupplier) {
        if (providerSupplier == null) {
            throw new IllegalArgumentException("providerSupplier is null");
        }

        this.providerSupplier = providerSupplier;
    }

    private E create() {
        final E result = this.providerSupplier.get();
        if (this.ecSupplier != null) {
            result.setExceptionCallback(this.ecSupplier.get());
        }
        if (this.executorSupplier != null) {
            if (result instanceof ExecutorAware) {
                final ExecutorAware ea = (ExecutorAware) result;
                ea.setExecutorService(this.executorSupplier.get());
            } else {
                throw new IllegalStateException(String.format(
                        "The configured EventProvider %s does not support setting an ExecutorService",
                        result));
            }
        }
        return result;
    }

    @Override
    public Final<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(ExceptionCallback ec) {
        this.ecSupplier = () -> ec;
        return new Final<AsyncProviderConfigurator<S, E>, E>() {

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
    public Final<AsyncProviderConfigurator<S, E>, E> exceptionCallBack(
            Supplier<ExceptionCallback> callBackSupplier) {
        if (callBackSupplier == null) {
            throw new IllegalArgumentException("callBackSupplier is null");
        }
        this.ecSupplier = callBackSupplier;
        return new Final<AsyncProviderConfigurator<S, E>, E>() {

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
    public Final<ProviderConfigurator<S, StatisticsEventProvider<S, E>>, StatisticsEventProvider<S, E>> statistics() {
        final Supplier<StatisticsEventProvider<S, E>> supplier =
                () -> new StatisticsEventProvider<S, E>(this.create());

        return new Final<ProviderConfigurator<S, StatisticsEventProvider<S, E>>, StatisticsEventProvider<S, E>>() {

            @Override
            public ProviderConfigurator<S, StatisticsEventProvider<S, E>> and() {
                return new ProviderConfiguratorImpl<S, StatisticsEventProvider<S, E>>(
                        supplier, AsyncProviderConfiguratorImpl.this.ecSupplier);
            }

            @Override
            public Supplier<StatisticsEventProvider<S, E>> createSupplier() {
                return supplier;
            }

            @Override
            public StatisticsEventProvider<S, E> create() {
                return supplier.get();
            }

        };
    }

    @Override
    public Final<AsyncProviderConfigurator<S, E>, E> executor(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor is null");
        }
        return executor(() -> executor);
    }

    @Override
    public Final<AsyncProviderConfigurator<S, E>, E> executor(
            Supplier<ExecutorService> executorSupplier) {
        if (executorSupplier == null) {
            throw new IllegalArgumentException("executorSupplier is null");
        }
        this.executorSupplier = executorSupplier;
        return new Final<AsyncProviderConfigurator<S, E>, E>() {

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
