package de.skuzzle.jeve;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;


/**
 * Implementation of basic {@link EventProvider} methods. All implementations are 
 * thread-safe.
 * 
 * @author Simon
 */
public abstract class AbstractEventProvider implements EventProvider {
    
    /**
     * Copies a list of listeners into a new list, casting each element to the target
     * listener type.
     * 
     * @param <T> Type of listeners in the result.
     * @param listeners List to copy.
     * @param listenerClass Target listener type.
     * @return A new typed list of listeners.
     */
    private static <T extends EventListener> List<T> copyList(List<Object> listeners, 
            Class<T> listenerClass) {
        final List<T> result = new ArrayList<>(listeners.size());
        listeners.forEach(l -> result.add(listenerClass.cast(l)));
        return result;
    }
    
    
    
    /** Holds the listener classes mapped to listener instances */
    protected final Map<Class<?>, List<Object>> listeners;
    
    
    
    /**
     * Creates a new {@link AbstractEventProvider}.
     */
    public AbstractEventProvider() {
        this.listeners = new HashMap<>();
    }

    
    
    @Override
    public <T extends EventListener> Listeners<T> getListeners(Class<T> listenerClass) {
        if (listenerClass == null) {
            throw new NullPointerException("listenerClass");
        }
        synchronized (this.listeners) {
            final List<Object> listeners = this.listeners.get(listenerClass);
            if (listeners == null) {
                return Listeners.empty(this, listenerClass);
            }
            return new Listeners<T>(
                copyList(listeners, listenerClass), listenerClass, this);
        }
    }
    
    
    
    @Override
    public <T extends EventListener> void clearAllListeners(Class<T> listenerClass) {
        synchronized (this.listeners) {
            this.listeners.remove(listenerClass);
        }
    }
    
    
    
    @Override
    public void clearAllListeners() {
        synchronized (this.listeners) {
            this.listeners.clear();
        }
    }
    

    
    @Override
    public <T extends EventListener> void addListener(Class<T> listenerClass, 
            T listener) {
        if (listenerClass == null) {
            throw new NullPointerException("listenerClass");
        } else if (listener == null) {
            throw new NullPointerException("listener");
        } else if (!listenerClass.isInstance(listener)) {
            throw new IllegalArgumentException("Listener " + listener + 
                    " is not of type " + listenerClass);
        }
        synchronized (this.listeners) {
            List<Object> listeners = this.listeners.get(listenerClass);
            if (listeners == null) {
                listeners = new LinkedList<>();
                this.listeners.put(listenerClass, listeners);
            }
            listeners.add(listener);
        }
    }


    
    @Override
    public <T extends EventListener> void removeListener(Class<T> listenerClass, 
            T listener) {
        if (listenerClass == null || listener == null) {
            return;
        }
        synchronized (this.listeners) {
            final List<Object> listeners = this.listeners.get(listenerClass);
            if (listeners == null) {
                return;
            }
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                this.listeners.remove(listenerClass, listeners);
            }
        }
    }
    
    
    
    @Override
    public <L extends EventListener, E extends Event<?>> void dispatch(
            Class<L> listenerClass, E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        if (this.canDispatch()) {
            this.notifyListeners(listenerClass, event, bc, ec);
        }
    }
    
    
    
    /**
     * Notifies all listeners registered for the provided class with the provided event.
     * This method is failure tolerant and will continue notifying listeners even if one
     * of them threw an exception. Exceptions are passed to the provided 
     * {@link ExceptionCallback}.
     * 
     * <p>This method does not check whether this provider is ready for dispatching and
     * might thus throw an exception when trying to dispatch an event while the provider
     * is not ready.</p>
     * 
     * @param <L> Type of the listeners which will be notified.
     * @param <E> Type of the event which will be passed to a listener.
     * @param listenerClass The class of listeners that should be notified.
     * @param event The event to pass to each listener.
     * @param bc The method of the listener to call.
     * @param ec The callback which gets notified about exceptions.
     * @return Returns <code>true</code> if all listeners have been notified successfully.
     *          Return <code>false</code> if one listener threw an exception.
     */
    protected <L extends EventListener, E extends Event<?>> boolean notifyListeners(
            Class<L> listenerClass, E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        
        boolean result = true;
        final Listeners<L> listeners = this.getListeners(listenerClass);
        for (L listener : listeners) {
            try {
                if (event.isHandled()) {
                    return result;
                }
                    
                bc.accept(listener, event);
                if (listener instanceof OneTimeEventListener) {
                    final OneTimeEventListener otl = (OneTimeEventListener) listener;
                    if (otl.workDone(this)) {
                        this.removeListener(listenerClass, listener);
                    }
                }
            } catch (RuntimeException e) {
                result = false;
                try {
                    ec.exception(e);
                } catch (Exception e1) {
                    // where is your god now?
                    e1.printStackTrace();
                }
            }
        }
        return result;
    }
    
    
    
    @Override
    public String toString() {
        return this.listeners.toString();
    }
}
