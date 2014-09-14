package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.builder.EventProviderConfigurator.Final;
import de.skuzzle.jeve.builder.EventProviderConfigurator.AsyncProviderConfigurator;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderChoser;
import de.skuzzle.jeve.builder.EventProviderConfigurator.ProviderConfigurator;
import de.skuzzle.jeve.providers.AWTEventProvider;
import de.skuzzle.jeve.providers.AsynchronousEventProvider;
import de.skuzzle.jeve.providers.ParallelEventProvider;
import de.skuzzle.jeve.providers.SynchronousEventProvider;

class ProviderChoserImpl<S extends ListenerStore> implements ProviderChoser<S> {

    private final Supplier<S> storeSupplier;

    public ProviderChoserImpl(Supplier<S> storeSupplier) {
        if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }

        this.storeSupplier = storeSupplier;
    }

    @Override
    public <C, E extends EventProvider<S>> Final<C, E> customProvider(
            CustomConfigurator<S, C, E> configurator) {
        if (configurator == null) {
            throw new IllegalArgumentException("configurator is null");
        }

        return new Final<C, E>() {

            @Override
            public C and() {
                return configurator.getConfigurator(ProviderChoserImpl.this.storeSupplier);
            }

            @Override
            public Supplier<E> asSupplier() {
                return () -> create();
            }

            @Override
            public E create() {
                return configurator.createNow(ProviderChoserImpl.this.storeSupplier);
            }
        };
    }

    private <E extends EventProvider<S>> Final<ProviderConfigurator<S, E>, E> synchronAnd(
            Supplier<E> supplier) {

        return new Final<ProviderConfigurator<S, E>, E>() {
            @Override
            public ProviderConfigurator<S, E> and() {
                return new ProviderConfiguratorImpl<S, E>(supplier);
            }

            @Override
            public Supplier<E> asSupplier() {
                return supplier;
            }

            @Override
            public E create() {
                return supplier.get();
            }
        };
    }

    private <E extends EventProvider<S>> Final<AsyncProviderConfigurator<S, E>, E> asynchronAnd(
            Supplier<E> supplier) {

        return new Final<AsyncProviderConfigurator<S, E>, E>() {
            @Override
            public AsyncProviderConfigurator<S, E> and() {
                return new AsyncProviderConfiguratorImpl<S, E>(supplier);
            }

            @Override
            public Supplier<E> asSupplier() {
                return supplier;
            }

            @Override
            public E create() {
                return supplier.get();
            }
        };
    }


    @Override
    public Final<ProviderConfigurator<S, SynchronousEventProvider<S>>, SynchronousEventProvider<S>> synchronousProvider() {
        final Supplier<SynchronousEventProvider<S>> supplier =
                () -> new SynchronousEventProvider<S>(this.storeSupplier.get());
        return synchronAnd(supplier);
    }

    @Override
    public Final<AsyncProviderConfigurator<S, AsynchronousEventProvider<S>>, AsynchronousEventProvider<S>> asynchronousProvider() {
        final Supplier<AsynchronousEventProvider<S>> supplier =
                () -> new AsynchronousEventProvider<S>(this.storeSupplier.get());
        return asynchronAnd(supplier);
    }

    @Override
    public Final<AsyncProviderConfigurator<S, ParallelEventProvider<S>>, ParallelEventProvider<S>> parallelProvider() {
        final Supplier<ParallelEventProvider<S>> supplier =
                () -> new ParallelEventProvider<S>(this.storeSupplier.get());
        return asynchronAnd(supplier);
    }

    @Override
    public Final<ProviderConfigurator<S, AWTEventProvider<S>>, AWTEventProvider<S>> waitingAWTEventProvider() {
        final Supplier<AWTEventProvider<S>> supplier =
                () -> new AWTEventProvider<S>(this.storeSupplier.get(), true);
        return synchronAnd(supplier);
    }

    @Override
    public Final<ProviderConfigurator<S, AWTEventProvider<S>>, AWTEventProvider<S>> asynchronAWTEventProvider() {
        final Supplier<AWTEventProvider<S>> supplier =
                () -> new AWTEventProvider<S>(this.storeSupplier.get(), false);
        return synchronAnd(supplier);
    }

}
