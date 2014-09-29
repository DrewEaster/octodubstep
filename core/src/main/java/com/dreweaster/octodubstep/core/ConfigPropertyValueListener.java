package com.dreweaster.octodubstep.core;

/**
 */
public interface ConfigPropertyValueListener<V> {

    void valueChanged(V oldValue, V newValue);
}
