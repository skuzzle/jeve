package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.providers.SequentialEventProvider;
import de.skuzzle.jeve.stores.DefaultListenerStore;

/**
 * Default implementation of jeve's fluent builder API.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
class EventProviderConfiguratorImpl implements EventProviderConfigurator {

    @Override
    public SequentialEventProvider<DefaultListenerStore> createInstantly() {
        return new SequentialEventProvider<DefaultListenerStore>(
                DefaultListenerStore.create());
    }

    @Override
    public ProviderChooser<DefaultListenerStore> defaultStore() {
        final Supplier<DefaultListenerStore> supplier = DefaultListenerStore::create;
        return new ProviderChooserImpl<DefaultListenerStore>(supplier);
    }

    @Override
    public <S extends ListenerStore> ProviderChooser<S> store(
            Supplier<S> storeSupplier) {
        if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }
        return new ProviderChooserImpl<S>(storeSupplier);
    }

    @Override
    public <S extends ListenerStore> ProviderChooser<S> store(S store) {
        if (store == null) {
            throw new IllegalArgumentException("store is null");
        }
        final Supplier<S> supplier = () -> store;
        return store(supplier);
    }

}
