package de.skuzzle.jeve;


/**
 * {@link EventProvider} implementation which is always ready for dispatching and simply
 * runs all listeners within the current thread.
 * 
 * @author Simon Taddiken
 */
class SynchronousEventProvider extends AbstractEventProvider {

    @Override
    public boolean isSequential() {
        return true;
    }
    
    
    
    @Override
    public boolean canDispatch() {
        return true;
    }
}
