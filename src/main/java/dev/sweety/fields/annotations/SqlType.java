package dev.sweety.fields.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author mk$weety
 * SqlType is an annotation used to define the SQL type of a database field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SqlType {

    /**
     * The SQL type of the field.
     *
     * @return the SQL type
     */
    String type();
}