package de.skuzzle.jeve;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * EventProvider implementation which uses an {@link ExecutorService} to notify each
 * listener within a dedicated thread. This implementation is thereby 
 * {@link #isSequential() not sequential}. 
 * 
 * <p>Instances of this class can be obtained using the static factory methods of the 
 * {@link EventProvider} interface.</p>
 * 
 * @author Simon Taddiken
 * @since 1.1.0
 */
public class ParallelEventProvider extends AbstractEventProvider {

    private final ExecutorService executor;
    
    /**
     * Creates a new ParallelEventPRovider.
     * 
     * @param executor The executor to use.
     */
    ParallelEventProvider(ExecutorService executor) {
        if (executor == null) {
            throw new IllegalArgumentException("executor is null");
        }
        this.executor = executor;
    }
    
    
    
    @Override
    public <L extends Listener, E extends Event<?>> void dispatch(Class<L> listenerClass,
            E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        this.checkDispatchArgs(listenerClass, event, bc, ec);
        
        if (!this.canDispatch()) {
            return;
        }
        
        final Listeners<L> listeners = this.getListeners(listenerClass);
        listeners.forEach(listener -> {
            try {
                this.executor.execute(() -> {
                    this.notifySingle(listenerClass, listener, event, bc, ec);
                });
            } catch (RuntimeException e) {
                this.handleException(ec, e, listener, event);
            }
        });
    }
    
    
    
    @Override
    public <L extends Listener, E extends Event<?>> void dispatch(Class<L> listenerClass,
            E event, BiFunction<L, E, Boolean> bf, ExceptionCallback ec) {
        throw new UnsupportedOperationException();
    }
    
    
    
    @Override
    public boolean canDispatch() {
        return !this.executor.isShutdown() && !this.executor.isTerminated();
    }
    
    
    
    @Override
    public boolean isSequential() {
        return false;
    }
    
    
    
    @Override
    public void close() {
        super.close();
        this.executor.shutdownNow();
        try {
            this.executor.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
