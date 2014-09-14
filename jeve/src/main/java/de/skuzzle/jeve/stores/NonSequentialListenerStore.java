package de.skuzzle.jeve.stores;

import de.skuzzle.jeve.ListenerStore;

public interface NonSequentialListenerStore extends ListenerStore {

    @Override
    public default boolean isSequential() {
        return false;
    }
}
