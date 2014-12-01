package de.skuzzle.jeve.builder;

import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.stores.DefaultListenerStore;

public class ListenerStoreConfiguratorImpl implements ListenerStoreConfigurator {

    private static class FinalImpl<S extends ListenerStore> implements
            Final<S, ConfigureThreadSafety<S>> {

        final S store;

        public FinalImpl(S store) {
            this.store = store;
        }

        @Override
        public ConfigureThreadSafety<S> and() {
            return new ConfigureThreadSafetyImpl<>(this.store);
        }

    }

    private static class ConfigureThreadSafetyImpl<S extends ListenerStore> implements
            ConfigureThreadSafety<S> {

        private final S store;

        public ConfigureThreadSafetyImpl(S store) {
            this.store = store;
        }

        @Override
        public Final<S, ConfigureThreadSafety<S>> threadSafe() {
            return new FinalImpl<>((S) this.store.synchronizedView());
        }

    }

    @Override
    public Final<DefaultListenerStore, ConfigureThreadSafety<DefaultListenerStore>> defaultStore() {
        return new FinalImpl<>(DefaultListenerStore.create());
    }

}
