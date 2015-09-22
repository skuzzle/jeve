package de.skuzzle.jeve.builder;

import java.util.function.Function;
import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ListenerSource;
import de.skuzzle.jeve.builder.EventProviderConfigurator.AsyncProviderConfigurator;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Chainable;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderChooser;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderConfigurator;
import de.skuzzle.jeve.providers.AWTEventProvider;
import de.skuzzle.jeve.providers.AsynchronousEventProvider;
import de.skuzzle.jeve.providers.BlockingParallelEventProvider;
import de.skuzzle.jeve.providers.ParallelEventProvider;
import de.skuzzle.jeve.providers.SynchronousEventProvider;
import de.skuzzle.jeve.providers.UnrollingEventProvider;

class ProviderChooserImpl implements ProviderChooser {

    private final Supplier<? extends ListenerSource> sourceSupplier;

    ProviderChooserImpl(Supplier<? extends ListenerSource> sourceSupplier) {
        if (sourceSupplier == null) {
            throw new IllegalArgumentException("sourceSupplier is null");
        }

        this.sourceSupplier = sourceSupplier;
    }

    @Override
    public <C, E extends EventProvider> Chainable<C, E> useCustomProvider(
            CustomConfigurator<C, E> configurator) {
        if (configurator == null) {
            throw new IllegalArgumentException("configurator is null");
        }

        return new Chainable<C, E>() {

            @Override
            public C and() {
                return configurator.getConfigurator(
                        ProviderChooserImpl.this.sourceSupplier);
            }

            @Override
            public Supplier<E> createSupplier() {
                return this::create;
            }

            @Override
            public E create() {
                return configurator.createNow(ProviderChooserImpl.this.sourceSupplier);
            }
        };
    }

    private <E extends EventProvider> Chainable<ProviderConfigurator<E>, E>
            synchronAnd(Function<ListenerSource, E> providerConstructor,
                    Supplier<? extends ListenerSource> storeSupplier) {

        return new Chainable<ProviderConfigurator<E>, E>() {
            @Override
            public ProviderConfigurator<E> and() {
                return new ProviderConfiguratorImpl<E>(providerConstructor,
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

    private <E extends EventProvider> Chainable<AsyncProviderConfigurator<E>, E>
            asynchronAnd(Function<ListenerSource, E> providerConstructor,
                    Supplier<? extends ListenerSource> storeSupplier) {

        return new Chainable<AsyncProviderConfigurator<E>, E>() {
            @Override
            public AsyncProviderConfigurator<E> and() {
                return new AsyncProviderConfiguratorImpl<E>(providerConstructor,
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
    public Chainable<ProviderConfigurator<SynchronousEventProvider>,
            SynchronousEventProvider> useSynchronousProvider() {
        final Function<ListenerSource, SynchronousEventProvider> ctor =
                SynchronousEventProvider::new;
        return synchronAnd(ctor, this.sourceSupplier);
    }

    @Override
    public Chainable<ProviderConfigurator<UnrollingEventProvider>,
            UnrollingEventProvider> useUnrollingProvider() {
        final Function<ListenerSource, UnrollingEventProvider> ctor =
                UnrollingEventProvider::new;
        return synchronAnd(ctor, this.sourceSupplier);
    }

    @Override
    public Chainable<AsyncProviderConfigurator<AsynchronousEventProvider>,
            AsynchronousEventProvider> useAsynchronousProvider() {
        final Function<ListenerSource, AsynchronousEventProvider> ctor =
                AsynchronousEventProvider::new;
        return asynchronAnd(ctor, this.sourceSupplier);
    }

    @Override
    public Chainable<AsyncProviderConfigurator<ParallelEventProvider>,
            ParallelEventProvider> useParallelProvider() {
        final Function<ListenerSource, ParallelEventProvider> ctor =
                ParallelEventProvider::new;
        return asynchronAnd(ctor, this.sourceSupplier);
    }

    @Override
    public Chainable<AsyncProviderConfigurator<BlockingParallelEventProvider>,
            BlockingParallelEventProvider> useBlockingParallelProvider() {
        final Function<ListenerSource, BlockingParallelEventProvider> ctor =
                BlockingParallelEventProvider::new;
        return asynchronAnd(ctor, this.sourceSupplier);
    }

    @Override
    public Chainable<ProviderConfigurator<AWTEventProvider>,
            AWTEventProvider> useWaitingAWTEventProvider() {
        final Function<ListenerSource, AWTEventProvider> ctor =
                store -> new AWTEventProvider(store, true);
        return synchronAnd(ctor, this.sourceSupplier);
    }

    @Override
    public Chainable<ProviderConfigurator<AWTEventProvider>,
            AWTEventProvider> useAsynchronAWTEventProvider() {
        final Function<ListenerSource, AWTEventProvider> ctor =
                store -> new AWTEventProvider(store, false);
        return synchronAnd(ctor, this.sourceSupplier);
    }
}
