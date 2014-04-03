package de.skuzzle.jeve;

import java.util.EventListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;




/**
 * 
 * @author Simon
 */
class AsynchronousEventProvider extends AbstractEventProvider {
    
    protected final ExecutorService dispatchPool;
    
    
    
    public AsynchronousEventProvider() {
        this(Executors.newFixedThreadPool(1));
    }
    
    
    
    public AsynchronousEventProvider(ExecutorService dispatcher) {
        super();
        this.dispatchPool = dispatcher;
    }
    
    
    
    @Override
    public <L extends EventListener, E extends Event<?>> void dispatch(
            Class<L> listenerClass, E event, BiConsumer<L, E> bc, ExceptionCallback ec) {
        
        if (this.canDispatch()) {
            this.dispatchPool.execute(() -> notifyListeners(listenerClass, event, bc, ec));
        }
        
    }
    
    
    
    @Override
    public boolean canDispatch() {
        return !this.dispatchPool.isShutdown() && !this.dispatchPool.isTerminated();
    }
    
    
    
    @Override
    public void dispose() {
        this.dispatchPool.shutdownNow();
        try {
            this.dispatchPool.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
