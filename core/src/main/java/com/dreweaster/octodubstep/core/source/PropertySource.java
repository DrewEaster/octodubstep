package com.dreweaster.octodubstep.core.source;

import com.google.common.base.Optional;

import java.io.IOException;

/**
 */
public interface PropertySource {

    String getDescription();

    Iterable<String> getPropertyNames();

    Optional<String> getValue(String name);

    boolean isReloadable();

    void reload() throws IOException;

    void addListener(PropertySourceListener listener);

    void removeListener(PropertySourceListener listener);
}
