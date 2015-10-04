package de.skuzzle.jeve.guice;

import com.google.inject.Injector;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

/**
 * A ListenerSource implementation that automatically looks up {@link Listener}
 * implementations from a Guice {@link Injector}. For this to work, the Injector
 * must be created with installing the {@link JeveModule} which serves for
 * indexing all Listener implementation types.
 * <p>
 * Implementations of this source will not be {@link #isSequential() sequential}
 * as the order in which types are encountered by Guice is neither predicatable
 * nor deterministic.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public interface GuiceListenerSource extends ListenerSource {

    public static GuiceListenerSource create(Injector injector) {
        if (injector == null) {
            throw new IllegalArgumentException("injector is null");
        }
        return new GuiceListenerSourceImpl(injector);
    }

    @Override
    public GuiceListenerSource synchronizedView();
}
