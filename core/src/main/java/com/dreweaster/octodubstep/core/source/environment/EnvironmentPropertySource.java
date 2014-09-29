package com.dreweaster.octodubstep.core.source.environment;

import com.google.common.base.Optional;
import com.dreweaster.octodubstep.core.source.PropertySource;
import com.dreweaster.octodubstep.core.source.PropertySourceListener;

import java.io.IOException;

public final class EnvironmentPropertySource implements PropertySource {

    @Override
    public String getDescription() {
        return "Environment";
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return System.getProperties().stringPropertyNames();
    }

    @Override
    public Optional<String> getValue(String name) {
        return Optional.fromNullable(System.getProperty(name));
    }

    @Override
    public boolean isReloadable() {
        return false;
    }

    @Override
    public void reload() throws IOException {
       // does nothing
    }

    @Override
    public void addListener(PropertySourceListener listener) {
       // does nothing
    }

    @Override
    public void removeListener(PropertySourceListener listener) {
       // does nothing
    }
}
