package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

@ListenerInterface(value = ListenerKind.NORMAL)
public interface ListenerFailNoInheritance {
    public void foo1(Event<String> e) {}
}