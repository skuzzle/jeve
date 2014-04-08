package de.skuzzle.jeve;


public class RegistrationEvent extends Event<EventProvider> {

    private final Class<? extends Listener> cls;
    
    
    RegistrationEvent(EventProvider source, Class<? extends Listener> cls) {
        super(source);
        this.cls = cls;
    }

    
    
    public Class<? extends Listener> getEventClass() {
        return this.cls;
    }
}
