package de.skuzzle.jeve;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
     * {@link SwingUtilities#invokeLater(Runnable)} or your current thread should wait
     * until all listeners are notified (uses 
     * {@link SwingUtilities#invokeAndWait(Runnable)} to run notify the listeners).
     * 
     * @param invokeNow If <code>true</code>, {@link #dispatch(Class, Event, BiConsumer, ExceptionCallback) dispatch} 
     *      uses <code>invokeAndWait</code>, otherwise <code>invokeLater</code>.
     */
    public AWTEventProvider(boolean invokeNow) {
        this.invokeNow = invokeNow;
    }
    
    
    
    @Override
    public <L extends Listener, E extends Event<?>> void dispatch(
            final Class<L> listenerClass, final E event, final BiConsumer<L, E> bc, 
            ExceptionCallback ec) {

        this.checkDispatchArgs(listenerClass, event, bc, ec);
        
        if (this.invokeNow) {
            if (SwingUtilities.isEventDispatchThread()) {
                notifyListeners(listenerClass, event, bc, ec);
            } else {
                try {
                    SwingUtilities.invokeAndWait(
                            () -> notifyListeners(listenerClass, event, bc, ec));
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            SwingUtilities.invokeLater(
                    () -> notifyListeners(listenerClass, event, bc, ec));
        }
    }
    
    
    
    @Override
    public <L extends Listener, E extends Event<?>> void dispatch(
            Class<L> listenerClass, E event, BiFunction<L, E, Boolean> bf,
            ExceptionCallback ec) {

        this.checkDispatchArgs(listenerClass, event, bf, ec);
        // canDispatch check missing as its always true
        
        if (this.invokeNow) {
            if (SwingUtilities.isEventDispatchThread()) {
                notifyListeners(listenerClass, event, bf, ec);
            } else {
                try {
                    SwingUtilities.invokeAndWait(
                            () -> notifyListeners(listenerClass, event, bf, ec));
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            SwingUtilities.invokeLater(
                    () -> notifyListeners(listenerClass, event, bf, ec));
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
