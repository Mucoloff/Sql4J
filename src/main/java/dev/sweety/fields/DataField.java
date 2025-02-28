package dev.sweety.fields;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 
 * @author mk$weety
 * DataField is an annotation used to define metadata for a database field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataField {

    /**
     * The name of the database field.
     *
     * @return the field name
     */
    String name() default "";

    /**
     * Indicates whether the field cannot be null.
     *
     * @return true if the field cannot be null, false otherwise
     */
    boolean notNull() default false;

    /**
     * Indicates whether the field must be unique.
     *
     * @return true if the field must be unique, false otherwise
     */
    boolean unique() default false;

    /**
     * The default value of the field.
     *
     * @return the default value
     */
    String value() default "";
}