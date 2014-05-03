package de.skuzzle.jeve;

import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;

@ListenerInterface(ListenerKind.MIXED)
public interface MixedListener extends Listener {

    public void foo(Event<String> e);
    
    public boolean foo2(Event<String> e);
}