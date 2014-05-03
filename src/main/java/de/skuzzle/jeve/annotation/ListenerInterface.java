package de.skuzzle.jeve.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface ListenerInterface {
    ListenerKind kind() default ListenerKind.NORMAL;
}