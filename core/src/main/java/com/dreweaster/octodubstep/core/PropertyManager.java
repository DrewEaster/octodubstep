package com.dreweaster.octodubstep.core;

/**
 */
public interface PropertyManager {

    <T> T propertiesFor(Class<T> providerClass);

    Iterable<Value<?>> properties();
}

