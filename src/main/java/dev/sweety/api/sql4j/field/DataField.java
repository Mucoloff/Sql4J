package dev.sweety.api.sql4j.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataField {

    String name() default "";

    boolean notNull() default false;

    boolean unique() default false;

    String value() default "";

}