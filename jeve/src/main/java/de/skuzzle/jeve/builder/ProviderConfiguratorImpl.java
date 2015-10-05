package de.skuzzle.jeve.builder;

import java.util.function.Function;
import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Chainable;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Final;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderConfigurator;
import de.skuzzle.jeve.providers.StatisticsEventProvider;

class ProviderConfiguratorImpl<S extends ListenerStore, E extends EventProvider<S>>
        implements ProviderConfigurator<S, E> {

    private final Function<S, E> providerConstructor;
    private final Supplier<S> storeSupplier;

    private Supplier<ExceptionCallback> ecSupplier;
    private boolean synchStore;

    ProviderConfiguratorImpl(Function<S, E> providerConstructor,
            Supplier<S> storeSupplier) {
        if (providerConstructor == null) {
            throw new IllegalArgumentException("providerSupplier is null");
        } else if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }

        this.providerConstructor = providerConstructor;
        this.storeSupplier = storeSupplier;
    }

    ProviderConfiguratorImpl(Function<S, E> providerConstructor,
            Supplier<S> storeSupplier,
            Supplier<ExceptionCallback> ecSupplier,
            boolean synchronizeStore) {

        this.providerConstructor = providerConstructor;
        this.storeSupplier = storeSupplier;
        this.ecSupplier = ecSupplier;
        this.synchStore = synchronizeStore;
    }

    @SuppressWarnings("unchecked")
    private E create() {
        final S store = this.synchStore
                ? (S) this.storeSupplier.get().synchronizedView()
                : this.storeSupplier.get();

        final E result = this.providerConstructor.apply(store);
        if (this.ecSupplier != null) {
            result.setExceptionCallback(this.ecSupplier.get());
        }
        return result;
    }

    @Override
    public Chainable<ProviderConfigurator<S, E>, E> exceptionCallBack(
            ExceptionCallback ec) {
        this.ecSupplier = () -> ec;
        return new Chainable<ProviderConfigurator<S, E>, E>() {

            @Override
            public ProviderConfigurator<S, E> and() {
                return ProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return ProviderConfiguratorImpl.this.create();
            }

        };
    }

    @Override
    public Chainable<ProviderConfigurator<S, E>, E> exceptionCallBack(
            Supplier<ExceptionCallback> callBackSupplier) {
        if (callBackSupplier == null) {
            throw new IllegalArgumentException("callBackSupplier is null");
        }
        this.ecSupplier = callBackSupplier;
        return new Chainable<ProviderConfigurator<S, E>, E>() {

            @Override
            public ProviderConfigurator<S, E> and() {
                return ProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return ProviderConfiguratorImpl.this.create();
            }

        };
    }

    @Override
    public Chainable<ProviderConfigurator<S, E>, E> synchronizeStore() {
        this.synchStore = true;
        return new Chainable<ProviderConfigurator<S, E>, E>() {

            @Override
            public ProviderConfigurator<S, E> and() {
                return ProviderConfiguratorImpl.this;
            }

            @Override
            public E create() {
                return ProviderConfiguratorImpl.this.create();
            }
        };
    }

    @Override
    public Final<StatisticsEventProvider<S, E>> statistics() {
        final Function<S, StatisticsEventProvider<S, E>> ctor = store -> {
            // XXX: passed store will be null here!
                    final E provider = ProviderConfiguratorImpl.this.create();
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
}
