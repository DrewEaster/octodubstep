package com.dreweaster.octodubstep.core.conversion;

/**
 */
public interface PropertyConverter<T> {

    Class<T> valueType();

    T convert(String value);
}
