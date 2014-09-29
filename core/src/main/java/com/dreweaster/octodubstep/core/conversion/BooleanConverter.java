package com.dreweaster.octodubstep.core.conversion;

/**
 */
public class BooleanConverter implements PropertyConverter<Boolean> {

    @Override
    public Class<Boolean> valueType() {
        return Boolean.class;
    }

    @Override
    public Boolean convert(String value) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        } else if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value: " + value);
    }
}
