package de.skuzzle.jeve.stores;

import java.util.HashSet;
import java.util.Set;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

/**
 *
 * @author Simon Taddiken
 * @since 2.1.0
 */
public abstract class AbstractListenerStore implements ListenerStore {

    public AbstractListenerStore() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Listener> void add(T listener) {
        final Set<Class<? extends Listener>> toAdd = new HashSet<>();
        handleClass(listener.getClass(), toAdd);
        toAdd.forEach(listenerClass -> add((Class<T>) listenerClass, listener));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Listener> void remove(T listener) {
        final Set<Class<? extends Listener>> toRemove = new HashSet<>();
        handleClass(listener.getClass(), toRemove);
        toRemove.forEach(listenerClass -> remove((Class<T>) listenerClass, listener));
    }

    @SuppressWarnings("unchecked")
    private void handleClass(Class<?> cls, Set<Class<? extends Listener>> result) {
        if (cls == null) {
            return;
        }
        for (final Class<?> interf : cls.getInterfaces()) {
            if (shouldAdd(interf)) {
                result.add((Class<? extends Listener>) interf);
            } else {
                handleClass(interf, result);
            }
        }
        handleClass(cls.getSuperclass(), result);
    }

    private boolean shouldAdd(Class<?> cls) {
        if (cls.isAnnotationPresent(ListenerInterface.class)) {
            final ListenerInterface li = cls.getAnnotation(ListenerInterface.class);
            if (li.value() == ListenerKind.TAGGING) {
                return false;
            }
        }
        return cls != Listener.class && Listener.class.isAssignableFrom(cls);

    }

}