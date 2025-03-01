package dev.sweety.annotations.table;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Info is an annotation used to define metadata for a table.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Info {
    /**
     * The name of the table.
     *
     * @return the table name
     */
    String name();
}
