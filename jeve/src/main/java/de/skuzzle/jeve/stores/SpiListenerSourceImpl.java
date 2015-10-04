package de.skuzzle.jeve.stores;

import java.util.ServiceLoader;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.skuzzle.jeve.Listener;

final class SpiListenerSourceImpl implements SpiListenerSource {

    private final ClassLoader classLoader;

    /**
     *
     * @param classLoader The ClassLoader to use for service look up.
     */
    SpiListenerSourceImpl(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    SpiListenerSourceImpl() {
        this(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public SpiListenerSourceImpl synchronizedView() {
        return this;
    }

    @Override
    public <L extends Listener> Stream<L> get(Class<L> listenerClass) {
        final Iterable<L> it = ServiceLoader.load(listenerClass, this.classLoader);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                it.iterator(), Spliterator.IMMUTABLE), false);
    }

    @Override
    public boolean isSequential() {
        return false;
    }

    @Override
    public void close() {}

}
