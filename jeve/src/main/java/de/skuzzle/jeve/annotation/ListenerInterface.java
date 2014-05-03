package de.skuzzle.jeve.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Simon Taddiken
 * @since 1.1.0
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ListenerInterface {
    ListenerKind value() default ListenerKind.NORMAL;
}