package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

@ListenerInterface(ListenerKind.ABORTABLE)
public interface AbortableListenerFailReturnType extends Listener {
    public void foo(Event<String> e);
}