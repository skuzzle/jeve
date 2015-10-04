package de.skuzzle.jeve.stores;

import java.util.ServiceLoader;

import de.skuzzle.jeve.ListenerSource;

/**
 * Listener source that obtains its listeners from the java
 * {@link ServiceLoader}.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
public interface SpiListenerSource extends ListenerSource {

    /**
     * Creates a listener source that uses the given ClassLoader for service
     * lookup.
     *
     * @param classLoader The ClassLoader.
     * @return The listener source.
     */
    public static SpiListenerSource create(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader is null");
        }
        return new SpiListenerSourceImpl(classLoader);
    }

    /**
     * Creates a listener source that uses the current thread's context
     * ClassLoader for service lookup.
     *
     * @return The listner source.
     */
    public static SpiListenerSource create() {
        return new SpiListenerSourceImpl();
    }

    @Override
    public SpiListenerSource synchronizedView();
}
