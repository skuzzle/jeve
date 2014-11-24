package de.skuzzle.jeve;

import java.util.Optional;

public class HandleableEvent<S, L extends Listener, O> extends Event<S, L> {

    private O reason;

    public HandlableEvent(S source, Class<L> listenerClass) {
        super(source, listenerClass);
    }
    
    public void setHandledWithReason(O reason) {
        setHandled(true);
        this.reason = reason;
    }
    
    @Override
    public void setHandled(boolean handled) {
        if (!handled) {
            reason = null;
        }
        super.setHandled(handled);
    }
    
    public Optional<O> getReason() {
        return Optional.ofNullable(reason);
    }
}
