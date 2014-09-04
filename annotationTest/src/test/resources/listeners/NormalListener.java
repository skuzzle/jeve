package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

@ListenerInterface(value = ListenerKind.NORMAL)
public interface NormalListener extends Listener {
    public void foo1(Event<?, ? extends Listener> e);
    public void foo2(Event<?, ? extends Listener> e) throws IllegalArgumentException;
}