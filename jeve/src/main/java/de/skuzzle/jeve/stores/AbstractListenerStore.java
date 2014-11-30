package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    /** The default size hint for {@link #createListenerList()} */
    protected static final int DEFAULT_SIZE_HINT = 8;

    /**
     * Returns the List object to store listeners in.
     *
     * @param sizeHint The expected initial size of the list to create.
     * @return A new List instance.
     */
    protected abstract <T> List<T> createListenerList(int sizeHint);

    /**
     * Returns the List object to store listeners in
     *
     * @return A new List instance.
     */
    protected final <T> List<T> createListenerList() {
        return createListenerList(DEFAULT_SIZE_HINT);
    }

    /**
     * Creates a collection from the given stream, casting each object to the
     * provided listener class.
     *
     * @param <T> Type of the listeners in the given list.
     * @param listenerClass The class of the objects in the provided list.
     * @param listeners The stream to obtain the listeners for the resulting
     *            collection from.
     * @param sizeHint Expected size of the input stream.
     * @return A typed copy of the list.
     */
    protected <T extends Listener> Collection<T> copyList(Class<T> listenerClass,
            Stream<Object> listeners, int sizeHint) {
        return listeners
                .map(obj -> listenerClass.cast(obj))
                .collect(Collectors.toCollection(() -> new ArrayList<>(sizeHint)));
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