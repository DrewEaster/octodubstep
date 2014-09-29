package com.dreweaster.octodubstep.core;

import com.google.common.base.Optional;
import com.dreweaster.octodubstep.core.conversion.PropertyConverter;
import com.dreweaster.octodubstep.core.source.PropertySource;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class PropertySourcePropertyManager implements PropertyManager {

    private static final Logger LOG = LoggerFactory.getLogger(PropertySourcePropertyManager.class);

    private Map<Class<?>, Object> configProviders = new HashMap<Class<?>, Object>();

    private Map<Method, Value<?>> configPropertyValues = new HashMap<Method, Value<?>>();

    private Map<Class<?>, PropertyConverter> converters = new HashMap<Class<?>, PropertyConverter>();

    public PropertySourcePropertyManager(
            List<Class<?>> propertyProviderClasses,
            List<PropertySource> propertySources,
            List<PropertyConverter<?>> propertyConverters) {

        for (PropertyConverter<?> converter : propertyConverters) {
            converters.put(converter.valueType(), converter);
        }

        Map<String, Map.Entry<PropertySource, String>> properties =
                new HashMap<String, Map.Entry<PropertySource, String>>();

        for (PropertySource propertySource : propertySources) {
            for (String propertyName : propertySource.getPropertyNames()) {
                if (!properties.containsKey(propertyName)) {
                    properties.put(propertyName, new AbstractMap.SimpleEntry<PropertySource, String>(
                            propertySource,
                            propertySource.getValue(propertyName).get()));
                }
            }
        }

        for (Class<?> propertyProviderClass : propertyProviderClasses) {

            for (Method method : propertyProviderClass.getMethods()) {
                Property configProperty = method.getAnnotation(Property.class);
                if (configProperty != null) {
                    ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
                    Type type = parameterizedType.getActualTypeArguments()[0];
                    if (type instanceof ParameterizedType) {
                        ParameterizedType embeddedParameterizedType = (ParameterizedType) type;
                        parsePropertyAsList(
                                properties,
                                method,
                                configProperty,
                                parameterizedType.getRawType().equals(DynamicValue.class),
                                embeddedParameterizedType);
                    } else {
                        parsePropertyAsSimpleType(
                                properties,
                                method,
                                configProperty,
                                parameterizedType.getRawType().equals(DynamicValue.class),
                                type);
                    }
                }
            }

            Object proxy = Proxy.newProxyInstance(
                    propertyProviderClass.getClassLoader(),
                    new Class[]{propertyProviderClass},
                    new ConfigPropertyInvocationHandler());

            configProviders.put(propertyProviderClass, proxy);

            // TODO: Don't fail to startup until all undefined required properties have been parsed
        }
    }

    @Override
    public Iterable<Value<?>> properties() {
        return configPropertyValues.values();
    }


    @Override
    public <T> T propertiesFor(Class<T> providerClass) {
        return (T) configProviders.get(providerClass);
    }

    private void parsePropertyAsSimpleType(
            Map<String, Map.Entry<PropertySource, String>> properties,
            Method method,
            Property configProperty,
            boolean dynamic,
            Type type) {
        PropertyConverter<?> converter = converters.get(type);
        Map.Entry<PropertySource, String> propertyEntry = properties.get(configProperty.name());
        Object value = propertyEntry != null
                ? converter.convert(propertyEntry.getValue())
                : !configProperty.required()
                ? converter.convert(configProperty.defaultValue())
                : null; // TODO: Need to throw error here as property is required!!!

        Optional<PropertySource> source = propertyEntry == null
                ? Optional.<PropertySource>absent()
                : Optional.of(propertyEntry.getKey());

        if (dynamic) {
            configPropertyValues.put(method, new PropertySourceDynamicConfigPropertyValue<Object>(
                    Optional.of(value),
                    configProperty,
                    ((Class) type).getName(),
                    source,
                    !source.isPresent()));
        } else {
            configPropertyValues.put(method, new PropertySourceConfigPropertyValue<Object>(
                    Optional.of(value),
                    configProperty,
                    ((Class) type).getName(),
                    source,
                    !source.isPresent()));
        }
    }

    private void parsePropertyAsList(
            Map<String, Map.Entry<PropertySource, String>> properties,
            Method method,
            Property configProperty,
            boolean dynamic,
            ParameterizedType embeddedParameterizedType) {

        if (List.class.equals(embeddedParameterizedType.getRawType())) {
            Map.Entry<PropertySource, String> propertyEntry = properties.get(configProperty.name());
            Class<?> listItemType = (Class) embeddedParameterizedType.getActualTypeArguments()[0];
            PropertyConverter<?> converter = converters.get(listItemType);
            List<Object> items = new ArrayList<Object>();
            String[] values = propertyEntry != null
                    ? propertyEntry.getValue().split(",")
                    : !configProperty.required()
                    ? configProperty.defaultValue().split(",")
                    : null; // TODO: Need to throw error here as property is required!!!

            for (String value : values) {
                items.add(converter.convert(value));
            }

            Optional<PropertySource> source = propertyEntry == null
                    ? Optional.<PropertySource>absent()
                    : Optional.of(propertyEntry.getKey());

            if(dynamic) {
                configPropertyValues.put(method, new PropertySourceDynamicConfigPropertyValue<List<Object>>(
                        Optional.of(items),
                        configProperty,
                        embeddedParameterizedType.toString(),
                        source,
                        !source.isPresent()));
            } else {
                configPropertyValues.put(method, new PropertySourceConfigPropertyValue<List<Object>>(
                        Optional.of(items),
                        configProperty,
                        embeddedParameterizedType.toString(),
                        source,
                        !source.isPresent()));
            }
        }
    }

    /**
     * Implementation of {@link DynamicValue}
     *
     * @param <T> the value type
     */
    private final class PropertySourceDynamicConfigPropertyValue<T> implements DynamicValue<T> {

        private Optional<T> currentValue;

        private PropertyMetadata metadata;

        private PropertySourceDynamicConfigPropertyValue(
                Optional<T> currentValue,
                Property configProperty,
                String type,
                Optional<PropertySource> source,
                boolean loadedFromDefaultValue) {
            this.currentValue = currentValue;
            this.metadata = new SimpleConfigPropertyMetadata(
                    configProperty, type, true, source, loadedFromDefaultValue);

            //LOG.info("Loaded config property: @(name = " + configProperty.name() + ", type = " + type + ", value = " + currentValue.orNull() + ", from = " + (!loadedFromDefaultValue ? source.get().getDescription() : "default value") + ")");
        }

        @Override
        public Optional<T> currentValue() {
            return currentValue;
        }

        @Override
        public Optional<DateTime> lastAccessed() {
            return null;
        }

        @Override
        public PropertyMetadata metadata() {
            return metadata;
        }

        @Override
        public void addListener(ValueListener<T> listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeListener(ValueListener<T> listener) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Implementation of {@link Value}
     *
     * @param <T> the value type
     */
    private final class PropertySourceConfigPropertyValue<T> implements Value<T> {

        private Optional<T> currentValue;

        private PropertyMetadata metadata;

        private PropertySourceConfigPropertyValue(
                Optional<T> currentValue,
                Property configProperty,
                String type,
                Optional<PropertySource> source,
                boolean loadedFromDefaultValue) {
            this.currentValue = currentValue;
            this.metadata = new SimpleConfigPropertyMetadata(
                    configProperty, type, false, source, loadedFromDefaultValue);
            //LOG.info("Loaded config property: @(name = " + configProperty.name() + ", type = " + type + ", value = " + currentValue.orNull() + ", from = " + (!loadedFromDefaultValue ? source.get().getDescription() : "default value") + ")");

        }

        @Override
        public Optional<T> currentValue() {
            return currentValue;
        }

        @Override
        public Optional<DateTime> lastAccessed() {
            return null;
        }

        @Override
        public PropertyMetadata metadata() {
            return metadata;
        }

/*        @Override
        public void addListener(ConfigPropertyValueListener<T> listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeListener(ConfigPropertyValueListener<T> listener) {
            throw new UnsupportedOperationException();
        }*/
    }

    /**
     * Represents metadata for a config property.
     */
    private final class SimpleConfigPropertyMetadata implements PropertyMetadata {

        private Property configProperty;

        private Optional<PropertySource> source;

        private String type;

        private boolean dynamic;

        private boolean loadedFromDefaultValue;

        private SimpleConfigPropertyMetadata(
                Property configProperty,
                String type,
                boolean dynamic,
                Optional<PropertySource> source,
                boolean loadedFromDefaultValue) {
            this.configProperty = configProperty;
            this.type = type;
            this.dynamic = dynamic;
            this.source = source;
            this.loadedFromDefaultValue = loadedFromDefaultValue;
        }

        @Override
        public String name() {
            return configProperty.name();
        }

        @Override
        public String type() {
            return type;
        }

        @Override
        public boolean dynamic() {
            return dynamic;
        }

        @Override
        public String defaultValue() {
            return configProperty.defaultValue();
        }

        @Override
        public boolean required() {
            return configProperty.required();
        }

        @Override
        public Optional<PropertySource> source() {
            return source;
        }

        @Override
        public boolean loadedFromDefaultValue() {
            return loadedFromDefaultValue;
        }
    }

    /**
     * Invocation handler.
     */
    private class ConfigPropertyInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            return configPropertyValues.get(method);
        }
    }
}
