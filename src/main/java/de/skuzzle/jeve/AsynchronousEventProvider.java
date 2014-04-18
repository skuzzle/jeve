package de.skuzzle.jeve;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;




/**
 * This EventProvider fires events asynchronously using an {@link ExecutorService} for
 * managing the creation of threads.
 * 
 * @author Simon
 */
class AsynchronousEventProvider extends AbstractEventProvider {
    
    /** Service which serves for creating and reusing threads.  */
    protected final ExecutorService dispatchPool;
    
    
    /**
     * Creates a new {@link AsynchronousEventProvider} which uses a single threaded
     * {@link ExecutorService}.
     */
    public AsynchronousEventProvider() {
        this(Executors.newFixedThreadPool(1));
    }
    
    
    
    /**
     * Creates a new {@link AsynchronousEventProvider} which uses the provided 
     * {@link ExecutorService} for event dispatching.
     * 
     * @param dispatcher ExecutorService to use.
     */
    public AsynchronousEventProvider(ExecutorService dispatcher) {
        if (dispatcher == null) {
            throw new NullPointerException();
        }
        this.dispatchPool = dispatcher;
    }
    
    
    
    @Override
    public <L extends Listener, E extends Event<?>> void dispatch(
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
    public boolean isSequential() {
        return true;
    }
    
    
    
    @Override
    public void close() {
        super.close();
        this.dispatchPool.shutdownNow();
        try {
            this.dispatchPool.awaitTermination(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
