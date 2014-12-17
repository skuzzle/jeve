package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.AsyncProviderConfigurator;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Final;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderChooser;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderConfigurator;
import de.skuzzle.jeve.providers.AWTEventProvider;
import de.skuzzle.jeve.providers.AsynchronousEventProvider;
import de.skuzzle.jeve.providers.ParallelEventProvider;
import de.skuzzle.jeve.providers.SynchronousEventProvider;
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
    public <C, E extends EventProvider<S>> Final<C, E> useCustomProvider(
            CustomConfigurator<S, C, E> configurator) {
        if (configurator == null) {
            throw new IllegalArgumentException("configurator is null");
        }

        return new Final<C, E>() {

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

    private <E extends EventProvider<S>> Final<ProviderConfigurator<S, E>, E>
            synchronAnd(Supplier<E> supplier) {

        return new Final<ProviderConfigurator<S, E>, E>() {
            @Override
            public ProviderConfigurator<S, E> and() {
                return new ProviderConfiguratorImpl<S, E>(supplier);
            }

            @Override
            public Supplier<E> createSupplier() {
                return supplier;
            }

            @Override
            public E create() {
                return supplier.get();
            }
        };
    }

    private <E extends EventProvider<S>> Final<AsyncProviderConfigurator<S, E>, E>
            asynchronAnd(Supplier<E> supplier) {

        return new Final<AsyncProviderConfigurator<S, E>, E>() {
            @Override
            public AsyncProviderConfigurator<S, E> and() {
                return new AsyncProviderConfiguratorImpl<S, E>(supplier);
            }

            @Override
            public Supplier<E> createSupplier() {
                return supplier;
            }

            @Override
            public E create() {
                return supplier.get();
            }
        };
    }

    @Override
    public Final<ProviderConfigurator<S, SynchronousEventProvider<S>>,
            SynchronousEventProvider<S>> useSynchronousProvider() {
        final Supplier<SynchronousEventProvider<S>> supplier =
                () -> new SynchronousEventProvider<S>(this.storeSupplier.get());
        return synchronAnd(supplier);
    }

    @Override
    public Final<ProviderConfigurator<S, UnrollingEventProvider<S>>,
            UnrollingEventProvider<S>> useUnrollingProvider() {
        final Supplier<UnrollingEventProvider<S>> supplier =
                () -> new UnrollingEventProvider<S>(this.storeSupplier.get());
        return synchronAnd(supplier);
    }

    @Override
    public Final<AsyncProviderConfigurator<S, AsynchronousEventProvider<S>>,
            AsynchronousEventProvider<S>> useAsynchronousProvider() {
        final Supplier<AsynchronousEventProvider<S>> supplier =
                () -> new AsynchronousEventProvider<S>(this.storeSupplier.get());
        return asynchronAnd(supplier);
    }

    @Override
    public Final<AsyncProviderConfigurator<S, ParallelEventProvider<S>>,
            ParallelEventProvider<S>> useParallelProvider() {
        final Supplier<ParallelEventProvider<S>> supplier =
                () -> new ParallelEventProvider<S>(this.storeSupplier.get());
        return asynchronAnd(supplier);
    }

    @Override
    public Final<ProviderConfigurator<S, AWTEventProvider<S>>,
            AWTEventProvider<S>> useWaitingAWTEventProvider() {
        final Supplier<AWTEventProvider<S>> supplier =
                () -> new AWTEventProvider<S>(this.storeSupplier.get(), true);
        return synchronAnd(supplier);
    }

    @Override
    public Final<ProviderConfigurator<S, AWTEventProvider<S>>,
            AWTEventProvider<S>> useAsynchronAWTEventProvider() {
        final Supplier<AWTEventProvider<S>> supplier =
                () -> new AWTEventProvider<S>(this.storeSupplier.get(), false);
        return synchronAnd(supplier);
    }

}
