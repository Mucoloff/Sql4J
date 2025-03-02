package dev.sweety.api.sql4j.adapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldAdapter {

    Class<? extends Adapter<?>> adapter();

}
