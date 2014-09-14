package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ListenerStore;

public interface CustomConfigurator<S extends ListenerStore, C, E extends EventProvider<S>> {

    C getConfigurator(Supplier<S> storeSupplier);

    E createNow(Supplier<S> storeSupplier);
}
