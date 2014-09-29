package com.dreweaster.octodubstep.core.conversion;

/**
 */
public class IntegerConverter implements PropertyConverter<Integer>{

    @Override
    public Class<Integer> valueType() {
        return Integer.class;
    }

    @Override
    public Integer convert(String value) {
        return Integer.parseInt(value);
    }
}
