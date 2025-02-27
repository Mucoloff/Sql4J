package dev.sweety.table.fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mk$weety
 * ForeignKey is an annotation used to define a foreign key constraint on a database field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ForeignKey {

    /**
     * The name of the referenced table.
     *
     * @return the table name
     */
    String table() default "";

    /**
     * The name of the referenced table's primary key.
     *
     * @return the primary key name
     */
    String tableId(); //todo
}