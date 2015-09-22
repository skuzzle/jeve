package de.skuzzle.jeve.providers;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;

import javax.swing.SwingUtilities;

import de.skuzzle.jeve.AbortionException;
import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.ListenerSource;

/**
 * {@link EventProvider} implementation that dispatches all events in the AWT
 * event thread.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
public class AWTEventProvider extends AbstractEventProvider {

    private final boolean invokeNow;

    /**
     * Creates a new AwtEventProvider. You can decide whether events shall be
     * scheduled for later execution via
     * {@link SwingUtilities#invokeLater(Runnable)} or your current thread
     * should wait until all listeners are notified (uses
     * {@link SwingUtilities#invokeAndWait(Runnable)} to run notify the
     * listeners).
     *
     * @param source Responsible for storing and retrieving listeners of this
     *            provider.
     * @param invokeNow If <code>true</code>,
     *            {@link #dispatch(Event, BiConsumer, ExceptionCallback)
     *            dispatch} uses <code>invokeAndWait</code>, otherwise
     *            <code>invokeLater</code>.
     */
    public AWTEventProvider(ListenerSource source, boolean invokeNow) {
        super(source);
        this.invokeNow = invokeNow;
    }

    /**
     * Whether this provider will block the current thread until dispatch is
     * done. When this method returns <code>false</code>, the notification of
     * Events is queued in the AWT event queue and {@code dispatch} returns
     * immediately.
     *
     * @return Whether this provider blocks the current thread upon dispatching.
     */
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
                } catch (final InvocationTargetException e) {
                    if (e.getTargetException() instanceof AbortionException) {
                        throw (AbortionException) e.getTargetException();
                    }

                    // this should not be reachable, as notifyListeners can not
                    // throw any other exceptions
                    throw new IllegalStateException(e);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
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
