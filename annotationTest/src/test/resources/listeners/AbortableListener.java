package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

@ListenerInterface(ListenerKind.ABORTABLE)
public interface AbortableListener extends Listener {
    public boolean foo(Event<String> e);
    public boolean foo2(Event<String> e) throws IllegalArgumentException;
}