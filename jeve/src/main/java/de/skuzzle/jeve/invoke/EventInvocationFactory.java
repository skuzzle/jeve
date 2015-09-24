package de.skuzzle.jeve.invoke;

import java.util.function.BiConsumer;

import de.skuzzle.jeve.Event;
import de.skuzzle.jeve.ExceptionCallback;
import de.skuzzle.jeve.Listener;

/**
 * Factory to create {@link EventInvocation} objects to be used for notifying a
 * single listener.
 *
 * @author Simon Taddiken
 * @since 4.0.0
 */
@FunctionalInterface
public interface EventInvocationFactory {

    <L extends Listener, E extends Event<?, L>> EventInvocation create(L listener,
            E event, BiConsumer<L, E> method, ExceptionCallback ec);
}
