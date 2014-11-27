package de.skuzzle.jeve.util;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.EventProvider;

public class DifferentStringEvent extends Event<EventProvider, DifferentStringListener> {

    private final String content;

    public DifferentStringEvent(EventProvider source, String content) {
        super(source, DifferentStringListener.class);
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
}
