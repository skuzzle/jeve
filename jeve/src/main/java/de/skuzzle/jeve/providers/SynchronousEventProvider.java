package de.skuzzle.jeve.providers;

import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ListenerStore;

/**
 * {@link EventProvider} implementation which is always ready for dispatching
 * and simply runs all listeners within the current thread.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
public class SynchronousEventProvider<S extends ListenerStore> extends
        AbstractEventProvider<S> {

    public SynchronousEventProvider(S store) {
        super(store);
    }

    @Override
    public boolean canDispatch() {
        return true;
    }

    @Override
    protected boolean isImplementationSequential() {
        return true;
    }
}
