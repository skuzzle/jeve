package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.providers.SynchronousEventProvider;
import de.skuzzle.jeve.stores.DefaultListenerStore;

/**
 * Default implementation of jeve's fluent builder API.
 * 
 * @author Simon Taddiken
 */
public class ConfiguratorImpl implements EventProviderConfigurator {

    @Override
    public SynchronousEventProvider<DefaultListenerStore> createInstantly() {
        return new SynchronousEventProvider<DefaultListenerStore>(
                new DefaultListenerStore());
    }

    @Override
    public With<ProviderChoser<DefaultListenerStore>> defaultStore() {
        final Supplier<DefaultListenerStore> supplier = DefaultListenerStore::new;
        return new With<ProviderChoser<DefaultListenerStore>>() {
            @Override
            public ProviderChoser<DefaultListenerStore> with() {
                return new ProviderChoserImpl<DefaultListenerStore>(supplier);
            }

        };
    }

    @Override
    public <S extends ListenerStore> With<ProviderChoser<S>> store(
            Supplier<S> storeSupplier) {
        if (storeSupplier == null) {
            throw new IllegalArgumentException("storeSupplier is null");
        }

        return new With<ProviderChoser<S>>() {
            @Override
            public ProviderChoser<S> with() {
                return new ProviderChoserImpl<S>(storeSupplier);
            }

        };
    }

    @Override
    public <S extends ListenerStore> With<ProviderChoser<S>> store(S store) {
        if (store == null) {
            throw new IllegalArgumentException("store is null");
        }
        final Supplier<S> supplier = () -> store;
        return store(supplier);
    }

}
