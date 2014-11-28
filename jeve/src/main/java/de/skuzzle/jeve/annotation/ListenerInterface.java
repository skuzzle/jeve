package de.skuzzle.jeve.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is a tagging annotation for interfaces which are to be used as
 * {@link de.skuzzle.jeve.Listener Listeners} in jeve. The corresponding
 * annotation processor performs some compile time checks for whether your
 * listener adheres to the general style of listener definitions in jeve.
 *
 * <ul>
 * <li>A listener should be defined as an interface</li>
 * <li>The listener interface must extend {@link de.skuzzle.jeve.Listener
 * Listener}</li>
 * <li>Every method defined within the interface must adhere to the
 * {@link ListenerKind} specified as value to this annotation</li>
 * </ul>
 *
 * <p>
 * When omitting the {@link #value()} attribute, the target class is expected to
 * contain only {@link ListenerKind#NORMAL normal} listening methods.
 * </p>
 *
 * As this is only a tagging annotation, it is perfectly fine to use listeners
 * without specifying this annotation.
 *
 * @author Simon Taddiken
 * @since 1.1.0
 * @see ListenerKind
 * @see de.skuzzle.jeve.Listener
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ListenerInterface {
    /**
     * Specifies the kind of listening methods expected in this
     * {@link de.skuzzle.jeve.Listener Listener} implementation. The default
     * value is {@link ListenerKind#NORMAL}.
     *
     * @return The kind of listening methods in this listener.
     */
    ListenerKind value() default ListenerKind.NORMAL;
}