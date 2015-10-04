package de.skuzzle.jeve.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import de.skuzzle.jeve.Listener;

final class TypeIndexer implements TypeListener {

    private final TypeIndex index;

    TypeIndexer(TypeIndex index) {
        this.index = index;
    }

    @Override
    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        final Class<?> raw = type.getRawType();
        for (final Class<?> intf : raw.getInterfaces()) {
            if (intf != Listener.class && Listener.class.isAssignableFrom(intf)) {
                this.index.addImplementor(intf, raw);
            }
        }
    }
}
