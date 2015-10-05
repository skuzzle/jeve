package de.skuzzle.jeve.builder;

import java.util.function.Function;
import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.AsyncProviderConfigurator;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Chainable;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderChooser;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderConfigurator;
import de.skuzzle.jeve.providers.AWTEventProvider;
import de.skuzzle.jeve.providers.AsynchronousEventProvider;
import de.skuzzle.jeve.providers.ParallelEventProvider;
import de.skuzzle.jeve.providers.SequentialEventProvider;
import de.skuzzle.jeve.providers.UnrollingEventProvider;

class ProviderChooserImpl<S extends ListenerStore> implements ProviderChooser<S> {

    private final Supplier<S> storeSupplier;

    ProviderChooserImpl(Supplier<S> storeSupplier) {
        if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }

        this.storeSupplier = storeSupplier;
    }

    @Override
    public <C, E extends EventProvider<S>> Chainable<C, E> useCustomProvider(
            CustomConfigurator<S, C, E> configurator) {
        if (configurator == null) {
            throw new IllegalArgumentException("configurator is null");
        }

        return new Chainable<C, E>() {

            @Override
            public C and() {
                return configurator.getConfigurator(
                        ProviderChooserImpl.this.storeSupplier);
            }

            @Override
            public Supplier<E> createSupplier() {
                return this::create;
            }

            @Override
            public E create() {
                return configurator.createNow(ProviderChooserImpl.this.storeSupplier);
            }
        };
    }

    private <E extends EventProvider<S>> Chainable<ProviderConfigurator<S, E>, E>
            synchronAnd(Function<S, E> providerConstructor, Supplier<S> storeSupplier) {

        return new Chainable<ProviderConfigurator<S, E>, E>() {
            @Override
            public ProviderConfigurator<S, E> and() {
                return new ProviderConfiguratorImpl<S, E>(providerConstructor,
                        storeSupplier);
            }

            @Override
            public Supplier<E> createSupplier() {
                return this::create;
            }

            @Override
            public E create() {
                return providerConstructor.apply(storeSupplier.get());
            }
        };
    }

    private <E extends EventProvider<S>> Chainable<AsyncProviderConfigurator<S, E>, E>
            asynchronAnd(Function<S, E> providerConstructor, Supplier<S> storeSupplier) {

        return new Chainable<AsyncProviderConfigurator<S, E>, E>() {
            @Override
            public AsyncProviderConfigurator<S, E> and() {
                return new AsyncProviderConfiguratorImpl<S, E>(providerConstructor,
                        storeSupplier);
            }

            @Override
            public Supplier<E> createSupplier() {
                return this::create;
            }

            @Override
            public E create() {
                return providerConstructor.apply(storeSupplier.get());
            }
        };
    }

    @Override
    public Chainable<ProviderConfigurator<S, SequentialEventProvider<S>>,
            SequentialEventProvider<S>> useSynchronousProvider() {
        final Function<S, SequentialEventProvider<S>> ctor =
                SequentialEventProvider<S>::new;
        return synchronAnd(ctor, this.storeSupplier);
    }

    @Override
    public Chainable<ProviderConfigurator<S, UnrollingEventProvider<S>>,
            UnrollingEventProvider<S>> useUnrollingProvider() {
        final Function<S, UnrollingEventProvider<S>> ctor =
                UnrollingEventProvider<S>::new;
        return synchronAnd(ctor, this.storeSupplier);
    }

    @Override
    public Chainable<AsyncProviderConfigurator<S, AsynchronousEventProvider<S>>,
            AsynchronousEventProvider<S>> useAsynchronousProvider() {
        final Function<S, AsynchronousEventProvider<S>> ctor =
                AsynchronousEventProvider<S>::new;
        return asynchronAnd(ctor, this.storeSupplier);
    }

    @Override
    public Chainable<AsyncProviderConfigurator<S, ParallelEventProvider<S>>,
            ParallelEventProvider<S>> useParallelProvider() {
        final Function<S, ParallelEventProvider<S>> ctor =
                ParallelEventProvider<S>::new;
        return asynchronAnd(ctor, this.storeSupplier);
    }

    @Override
    public Chainable<ProviderConfigurator<S, AWTEventProvider<S>>,
            AWTEventProvider<S>> useWaitingAWTEventProvider() {
        final Function<S, AWTEventProvider<S>> ctor =
                store -> new AWTEventProvider<>(store, true);
        return synchronAnd(ctor, this.storeSupplier);
    }

    @Override
    public Chainable<ProviderConfigurator<S, AWTEventProvider<S>>,
            AWTEventProvider<S>> useAsynchronAWTEventProvider() {
        final Function<S, AWTEventProvider<S>> ctor =
                store -> new AWTEventProvider<>(store, false);
        return synchronAnd(ctor, this.storeSupplier);
    }
}
