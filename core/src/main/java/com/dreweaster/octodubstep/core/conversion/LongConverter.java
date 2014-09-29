package com.dreweaster.octodubstep.core.conversion;

/**
 */
public class LongConverter implements PropertyConverter<Long> {

    @Override
    public Class<Long> valueType() {
        return Long.class;
    }

    @Override
    public Long convert(String value) {
        return Long.parseLong(value);
    }
}
