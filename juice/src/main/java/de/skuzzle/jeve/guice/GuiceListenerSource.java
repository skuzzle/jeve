package de.skuzzle.jeve.guice;

import com.google.inject.Injector;

import de.skuzzle.jeve.ListenerSource;

public interface GuiceListenerSource extends ListenerSource {

    public static ListenerSource create(Injector injector) {
        if (injector == null) {
            throw new IllegalArgumentException("injector is null");
        }
        return new GuiceListenerSourceImpl(injector);
    }

    @Override
    public GuiceListenerSource synchronizedView();
}
