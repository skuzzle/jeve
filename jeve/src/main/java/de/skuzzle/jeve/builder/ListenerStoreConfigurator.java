package de.skuzzle.jeve.builder;

import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.stores.DefaultListenerStore;

public interface ListenerStoreConfigurator {

    public interface Final<S extends ListenerStore, C> {
        public C and();
    }

    public interface ConfigureThreadSafety<S extends ListenerStore> {

        public Final<S, ConfigureThreadSafety<S>> threadSafe();
    }

    public Final<DefaultListenerStore, ConfigureThreadSafety<DefaultListenerStore>> defaultStore();
}
