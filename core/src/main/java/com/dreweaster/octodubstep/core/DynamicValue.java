package com.dreweaster.octodubstep.core;

/**
 */
public interface DynamicValue<V> extends Value<V> {

    void addListener(ValueListener<V> listener);

    void removeListener(ValueListener<V> listener);
}
