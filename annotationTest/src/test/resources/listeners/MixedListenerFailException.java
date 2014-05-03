package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;
import java.io.IOException;

@ListenerInterface(ListenerKind.MIXED)
public interface MixedListenerFailException extends Listener {
    public void foo(Event<String> e) throws IOException;
    public boolean foo2(Event<String> e);
}