package com.dreweaster.octodubstep.core;

import com.dreweaster.octodubstep.core.conversion.*;
import com.dreweaster.octodubstep.core.source.PropertySource;

import java.util.ArrayList;
import java.util.List;

public class Octodubstep {

    private static final PropertyConverter<?>[] DEFAULT_CONVERTERS = {
            new StringConverter(),
            new IntegerConverter(),
            new LongConverter(),
            new BooleanConverter()
    };

    public static Builder newPropertyManager() {
        return new Builder();
    }

    public static class Builder {

        private List<PropertyConverter<?>> converters = new ArrayList<PropertyConverter<?>>();

        private List<PropertySource> propertySources = new ArrayList<PropertySource>();

        private List<Class<?>> providers = new ArrayList<Class<?>>();

        public Builder withProvider(Class<?> clazz) {
            providers.add(clazz);
            return this;
        }

        public Builder withPropertySource(PropertySource source) {
            propertySources.add(source);
            return this;
        }

        public Builder usingDefaultConverters() {
            converters.addAll(defaultConverters());
            return this;
        }

        public PropertyManager build() {
            return new PropertySourcePropertyManager(providers, propertySources, converters);
        }
    }

    private static List<PropertyConverter<?>> defaultConverters() {
        List<PropertyConverter<?>> converters = new ArrayList<PropertyConverter<?>>();
        for (PropertyConverter<?> converter : DEFAULT_CONVERTERS) {
            converters.add(converter);
        }
        return converters;
    }
}
