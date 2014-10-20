package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Final;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderConfigurator;
import de.skuzzle.jeve.providers.StatisticsEventProvider;

class ProviderConfiguratorImpl<S extends ListenerStore, E extends EventProvider<S>>
        implements ProviderConfigurator<S, E> {

    private final Supplier<E> providerSupplier;

    private Supplier<ExceptionCallback> ecSupplier;

    ProviderConfiguratorImpl(Supplier<E> providerSupplier) {
        if (providerSupplier == null) {
            throw new IllegalArgumentException("providerSupplier is null");
        }

        this.providerSupplier = providerSupplier;
    }

    ProviderConfiguratorImpl(Supplier<E> providerSupplier,
            Supplier<ExceptionCallback> ecSupplier) {

        this.providerSupplier = providerSupplier;
        this.ecSupplier = ecSupplier;
    }

    private E create() {
        final E result = this.providerSupplier.get();
        if (this.ecSupplier != null) {
            result.setExceptionCallback(this.ecSupplier.get());
        }
        return result;
    }

    @Override
    public Final<ProviderConfigurator<S, E>, E> exceptionCallBack(ExceptionCallback ec) {
        this.ecSupplier = () -> ec;
        return new Final<ProviderConfigurator<S, E>, E>() {

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
    public Final<ProviderConfigurator<S, E>, E> exceptionCallBack(
            Supplier<ExceptionCallback> callBackSupplier) {
        if (callBackSupplier == null) {
            throw new IllegalArgumentException("callBackSupplier is null");
        }
        this.ecSupplier = callBackSupplier;
        return new Final<ProviderConfigurator<S, E>, E>() {

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
    public Final<ProviderConfigurator<S, StatisticsEventProvider<S, E>>, StatisticsEventProvider<S, E>> statistics() {
        final Supplier<StatisticsEventProvider<S, E>> supplier =
                () -> new StatisticsEventProvider<S, E>(this.providerSupplier.get());

        return new Final<ProviderConfigurator<S, StatisticsEventProvider<S, E>>, StatisticsEventProvider<S, E>>() {

            @Override
            public ProviderConfigurator<S, StatisticsEventProvider<S, E>> and() {
                return new ProviderConfiguratorImpl<S, StatisticsEventProvider<S, E>>(
                        supplier, ProviderConfiguratorImpl.this.ecSupplier);
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
}
