package com.dreweaster.octodubstep.core;

/**
 */
public interface PropertyManager {

    <T> T getConfigProvider(Class<T> providerClass);

    Iterable<Value<?>> properties();
}

