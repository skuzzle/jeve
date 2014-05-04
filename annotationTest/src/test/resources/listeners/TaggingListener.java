package de.skuzzle.jeve.listeners;

import de.skuzzle.jeve.Listener;
import de.skuzzle.jeve.annotation.ListenerInterface;
import de.skuzzle.jeve.annotation.ListenerKind;


@ListenerInterface(ListenerKind.TAGGING)
public interface TaggingListener extends Listener {
    // should not produce any warning
}