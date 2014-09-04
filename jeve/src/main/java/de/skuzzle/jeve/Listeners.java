package de.skuzzle.jeve;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Collection class for {@link Listener}s. Despite being a normal collection,
 * this class knows what kind of listeners it contains and is thus able to
 * remove a certain listener from its parent {@link EventProvider}.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 * @param <T> Type of listeners that are contained in this collection.
 */
public class Listeners<T extends Listener> extends AbstractCollection<T> {

    private final Collection<T> backend;
    private final Class<T> eventClass;
    private final EventProvider parent;

    /**
     * Creates a new collection of listeners.
     *
     * @param c The listeners to be contained in this collection.
     * @param eventClass The class for which those listeners are registered at
     *            their parent {@link EventProvider}.
     * @param parent The parent EventProvider.
     */
    Listeners(Collection<T> c, Class<T> eventClass, EventProvider parent) {
        this.backend = c;
        this.eventClass = eventClass;
        this.parent = parent;
    }

    /**
     * Creates an empty listener list.
     *
     * @param <T> Type of the listeners.
     * @param parent The parent event provider.
     * @param eventClass The event class.
     * @return An empty Listeners instance.
     */
    public static <T extends Listener> Listeners<T> empty(EventProvider parent,
            Class<T> eventClass) {
        return new Listeners<>(Collections.emptyList(), eventClass, parent);
    }

    /**
     * Removes the provided listener from its parent.
     *
     * @param listener The listener to remove.
     */
    public void removeFromParent(T listener) {
        this.parent.removeListener(this.eventClass, listener);
    }

    @Override
    public Iterator<T> iterator() {
        return this.backend.iterator();
    }

    @Override
    public int size() {
        return this.backend.size();
    }
}
