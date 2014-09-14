package de.skuzzle.jeve.providers;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;

import javax.swing.SwingUtilities;

import de.skuzzle.jeve.AbortionException;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerStore;

/**
 * {@link EventProvider} implementation that dispatches all events in the AWT
 * event thread.
 *
 * @param <S> The type of the ListenerStore this provider uses.
 * @author Simon Taddiken
 * @since 1.0.0
 */
public class AWTEventProvider<S extends ListenerStore> extends AbstractEventProvider<S> {

    private final boolean invokeNow;

    /**
     * Creates a new AwtEventProvider. You can decide whether events shall be
     * scheduled for later execution via
     * {@link SwingUtilities#invokeLater(Runnable)} or your current thread
     * should wait until all listeners are notified (uses
     * {@link SwingUtilities#invokeAndWait(Runnable)} to run notify the
     * listeners).
     *
     * @param store Responsible for storing and retrieving listeners of this
     *            provider.
     * @param invokeNow If <code>true</code>,
     *            {@link #dispatch(Event, BiConsumer, ExceptionCallback)
     *            dispatch} uses <code>invokeAndWait</code>, otherwise
     *            <code>invokeLater</code>.
     */
    public AWTEventProvider(S store, boolean invokeNow) {
        super(store);
        this.invokeNow = invokeNow;
    }

    public boolean isInvokeNow() {
        return this.invokeNow;
    }

    @Override
    public <L extends Listener, E extends Event<?, L>> void dispatch(
            final E event, final BiConsumer<L, E> bc, ExceptionCallback ec) {

        checkDispatchArgs(event, bc, ec);

        if (this.invokeNow) {
            if (SwingUtilities.isEventDispatchThread()) {
                notifyListeners(event, bc, ec);
            } else {
                try {
                    SwingUtilities.invokeAndWait(
                            () -> notifyListeners(event, bc, ec));
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof AbortionException) {
                        throw (AbortionException) e.getTargetException();
                    }

                    // this should not be reachable, as notifyListeners can not
                    // throw any other exceptions
                    throw new AbortionException(e);
                } catch (InterruptedException e) {
                    throw new AbortionException(e);
                }
            }
        } else {
            SwingUtilities.invokeLater(() -> notifyListeners(event, bc, ec));
        }
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
