package dev.sweety.annotations.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mk$weety
 * PrimaryKey is an annotation used to define a primary key constraint on a database field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {

    /**
     * Indicates whether the primary key should auto-increment.
     *
     * @return true if the primary key should auto-increment, false otherwise
     */
    boolean autoIncrement() default false;
}