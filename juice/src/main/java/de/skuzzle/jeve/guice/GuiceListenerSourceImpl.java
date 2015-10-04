package de.skuzzle.jeve.guice;

import java.util.stream.Stream;

import javax.inject.Inject;

import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.guice.JeveModule.TestToken;

final class GuiceListenerSourceImpl implements GuiceListenerSource {

    private final Injector injector;
    private final TypeIndex typeIndex;

    @Inject
    GuiceListenerSourceImpl(Injector injector) {
        checkInjectorSetup(injector);
        this.injector = injector;
        this.typeIndex = injector.getInstance(TypeIndex.class);
    }

    private void checkInjectorSetup(Injector injector) {
        final Binding<?> binding = injector.getExistingBinding(
                Key.get(TestToken.class));
        if (binding == null) {
            throw new IllegalStateException(String.format(
                    "juice has not been configured correctly. " +
                    "In order to use GuiceListenerSource with the Injector '%s' you " +
                    "need to either install the JeveModule in any of your own modules " +
                    "or specify it when constructing the Injector",
                    injector));
        }
    }

    @Override
    public GuiceListenerSource synchronizedView() {
        return this;
    }

    @Override
    public <L extends Listener> Stream<L> get(Class<L> listenerClass) {
        return this.typeIndex.findImplementorsOf(listenerClass)
                .map(this.injector::getInstance);
    }

    @Override
    public boolean isSequential() {
        return false;
    }

    @Override
    public void close() {}

}
