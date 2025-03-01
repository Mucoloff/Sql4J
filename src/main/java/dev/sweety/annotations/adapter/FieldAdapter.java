package dev.sweety.annotations.adapter;

import dev.sweety.api.Adapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldAdapter {
    Class<? extends Adapter<?>> adapter();
}
