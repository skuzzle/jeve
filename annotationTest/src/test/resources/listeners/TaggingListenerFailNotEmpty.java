package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

@ListenerInterface(ListenerKind.TAGGING)
public interface TaggingListenerFailNotEmpty extends Listener {
    public void foo(Event<String> e);
}