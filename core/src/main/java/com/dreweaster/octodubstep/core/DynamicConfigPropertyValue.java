package com.dreweaster.octodubstep.core;

/**
 */
public interface DynamicConfigPropertyValue<V> extends ConfigPropertyValue<V> {

    void addListener(ConfigPropertyValueListener<V> listener);

    void removeListener(ConfigPropertyValueListener<V> listener);
}
