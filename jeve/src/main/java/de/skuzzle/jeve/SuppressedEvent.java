package de.skuzzle.jeve;

public interface SuppressedEvent {

    public Event<?, ?> getEvent();

    public Class<? extends Listener> getListenerClass();

    public void redispatch(EventProvider<?> provider);
}