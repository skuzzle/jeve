package de.skuzzle.jeve.stores;

import java.util.ArrayList;
import java.util.HashMap;

import de.skuzzle.jeve.ListenerStore;

/**
 * Sequential {@link ListenerStore} implementation. This class implements the
 * default semantics of a ListenerStore with no additional features. The public
 * interface to this store is thread safe.
 *
 * <p>
 * Performance notes: This store uses a {@link HashMap} of {@link ArrayList
 * ArrayLists} to manage the Listeners. Thus, adding a Listener performs in
 * {@code O(1)} and removing in {@code O(n)} where {@code n} is the number of
 * Listeners registered for the class for which the Listener should be removed.
 * The {@link #get(Class) get} method retrieves the stored listeners from a map
 * in {@code O(1)} but then needs to create a copy of this list in order to
 * avoid concurrency problems. It therefore performs in {@code O(n)}.
 * </p>
 *
 * @author Simon Taddiken
 * @since 2.0.0
 */
public interface DefaultListenerStore extends ListenerStore {

    public static DefaultListenerStore create() {
        return new DefaultListenerStoreImpl();
    }

}
