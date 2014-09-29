package com.dreweaster.octodubstep.core;

import com.dreweaster.octodubstep.core.conversion.BooleanConverter;
import com.dreweaster.octodubstep.core.conversion.IntegerConverter;
import com.dreweaster.octodubstep.core.conversion.LongConverter;
import com.dreweaster.octodubstep.core.conversion.PropertyConverter;
import com.dreweaster.octodubstep.core.conversion.StringConverter;
import com.dreweaster.octodubstep.core.source.PropertySource;

import java.util.ArrayList;
import java.util.List;

/**
 */
public final class DefaultPropertySourceConfigPropertyManager extends PropertySourceConfigPropertyManager {

    private static final PropertyConverter<?>[] DEFAULT_CONVERTERS = {
            new StringConverter(),
            new IntegerConverter(),
            new LongConverter(),
            new BooleanConverter()
    };

    public DefaultPropertySourceConfigPropertyManager(
            List<Class<?>> propertyProviderClasses,
            List<PropertySource> propertySources) {

        super(propertyProviderClasses, propertySources, defaultConverters());
    }

    public static List<PropertyConverter<?>> defaultConverters() {
        List<PropertyConverter<?>> converters = new ArrayList<PropertyConverter<?>>();
        for (PropertyConverter<?> converter : DEFAULT_CONVERTERS) {
            converters.add(converter);
        }
        return converters;
    }
}
