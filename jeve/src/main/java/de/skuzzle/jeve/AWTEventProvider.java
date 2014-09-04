package de.skuzzle.jeve;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;

import javax.swing.SwingUtilities;

/**
 * {@link EventProvider} implementation that dispatches all events in the AWT event
 * thread.
 *
 * @author Simon Taddiken
 * @since 1.0.0
 */
class AWTEventProvider extends AbstractEventProvider {

    private final boolean invokeNow;

    /**
     * Creates a new AwtEventProvider. You can decide whether events shall be
     * scheduled for later execution via
     * {@link SwingUtilities#invokeLater(Runnable)} or your current thread
     * should wait until all listeners are notified (uses
     * {@link SwingUtilities#invokeAndWait(Runnable)} to run notify the
     * listeners).
     *
     * @param invokeNow If <code>true</code>,
     *            {@link #dispatch(Event, BiConsumer, ExceptionCallback)
     *            dispatch} uses <code>invokeAndWait</code>, otherwise
     *            <code>invokeLater</code>.
     */
    public AWTEventProvider(boolean invokeNow) {
        this.invokeNow = invokeNow;
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
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            SwingUtilities.invokeLater(() -> notifyListeners(event, bc, ec));
        }
    }



    @Override
    public boolean isSequential() {
        return true;
    }



    @Override
    public boolean canDispatch() {
        return true;
    }
}
