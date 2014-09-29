package com.dreweaster.octodubstep.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigProperty {

    String name();

    String defaultValue() default "";

    boolean required() default true;

    //Class<? extends ConfigPropertyValidator> validator() default DoNothingValidator.class;
}
