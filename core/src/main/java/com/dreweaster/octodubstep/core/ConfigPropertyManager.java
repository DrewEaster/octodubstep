package com.dreweaster.octodubstep.core;

/**
 */
public interface ConfigPropertyManager {

    <T> T getConfigProvider(Class<T> providerClass);

    Iterable<ConfigPropertyValue<?>> properties();
}

