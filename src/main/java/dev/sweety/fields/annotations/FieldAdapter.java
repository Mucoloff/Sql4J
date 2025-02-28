package dev.sweety.fields.annotations;

import dev.sweety.fields.Adapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldAdapter {
    Class<? extends Adapter<?>> clazz();
}
