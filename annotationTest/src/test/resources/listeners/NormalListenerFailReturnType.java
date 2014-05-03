package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

@ListenerInterface(ListenerKind.NORMAL)
public interface NormalListenerFailReturnType extends Listener {
    public int foo(Event<String> e);
}