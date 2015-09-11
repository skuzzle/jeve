package de.skuzzle.jeve.builder;

import java.util.function.Supplier;

import de.skuzzle.jeve.ListenerSource;

/**
 * Default implementation of jeve's fluent builder API.
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
class EventProviderConfiguratorImpl implements EventProviderConfigurator {

    @Override
    public ProviderChooser store(Supplier<? extends ListenerSource> sourceSupplier) {
        if (sourceSupplier == null) {
            throw new IllegalArgumentException("sourceSupplier is null");
        }
        return new ProviderChooserImpl(sourceSupplier);
    }

    @Override
    public <S extends ListenerSource> ProviderChooser store(S source) {
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        }
        final Supplier<S> supplier = () -> source;
        return store(supplier);
    }

}
