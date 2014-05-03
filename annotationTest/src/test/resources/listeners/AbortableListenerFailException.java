package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;
import java.io.IOException;

@ListenerInterface(ListenerKind.ABORTABLE)
public interface AbortableListenerFailException extends Listener {
    public boolean foo(Event<String> e) throws IOException;
}