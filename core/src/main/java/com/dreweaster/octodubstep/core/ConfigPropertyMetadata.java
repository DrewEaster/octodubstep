package com.dreweaster.octodubstep.core;

import com.google.common.base.Optional;
import com.dreweaster.octodubstep.core.source.PropertySource;

/**
 */
public interface ConfigPropertyMetadata {

    String name();

    String type();

    boolean dynamic();

    String defaultValue();

    boolean required();

    //Optional<Class<? extends ConfigPropertyValidator>> validatorClass();

    boolean loadedFromDefaultValue();

    Optional<PropertySource> source();
}
