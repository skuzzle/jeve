package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.providers.SynchronousEventProvider;
import de.skuzzle.jeve.stores.DefaultListenerStore;

/**
 * Default implementation of jeve's fluent builder API.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
class EventProviderConfiguratorImpl implements EventProviderConfigurator {

    @Override
    public SynchronousEventProvider createInstantly() {
        return new SynchronousEventProvider(DefaultListenerStore.create());
    }

    @Override
    public ProviderChooser defaultStore() {
        final Supplier<ListenerStore> supplier = DefaultListenerStore::create;
        return new ProviderChooserImpl(supplier);
    }

    @Override
    public ProviderChooser store(Supplier<? extends ListenerStore> storeSupplier) {
        if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }
        return new ProviderChooserImpl(storeSupplier);
    }

    @Override
    public <S extends ListenerStore> ProviderChooser store(S store) {
        if (store == null) {
            throw new IllegalArgumentException("store is null");
        }
        final Supplier<S> supplier = () -> store;
        return store(supplier);
    }

}
