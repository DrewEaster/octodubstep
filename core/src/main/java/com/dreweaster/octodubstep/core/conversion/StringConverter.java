package com.dreweaster.octodubstep.core.conversion;

/**
 */
public class StringConverter implements PropertyConverter<String> {

    @Override
    public Class<String> valueType() {
        return String.class;
    }

    @Override
    public String convert(String value) {
        return value;
    }
}
