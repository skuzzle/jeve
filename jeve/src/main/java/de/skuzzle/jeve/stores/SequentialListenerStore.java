package de.skuzzle.jeve.stores;

import de.skuzzle.jeve.ListenerStore;

public interface SequentialListenerStore extends ListenerStore {

    @Override
    public default boolean isSequential() {
        return true;
    }
}
