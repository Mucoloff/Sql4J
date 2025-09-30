package dev.sweety.sql4j.api.adapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FieldAdapter {

    Class<? extends Adapter<?>> adapter();

}
