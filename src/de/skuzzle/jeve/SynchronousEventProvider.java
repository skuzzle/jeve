package de.skuzzle.jeve;


class SynchronousEventProvider extends AbstractEventProvider {

    @Override
    public boolean canDispatch() {
        return true;
    }
    
    

    @Override
    public void dispose() {}
}
