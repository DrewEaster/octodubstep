package com.dreweaster.octodubstep.core.source.file;

import com.google.common.base.Optional;
import com.dreweaster.octodubstep.core.source.PropertySource;
import com.dreweaster.octodubstep.core.source.PropertySourceListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 */
public final class PropertiesFileBasedPropertySource implements PropertySource {

    private ExternalFile externalFile;

    private Properties properties;

    /**
     * Constructor.
     *
     * @param externalFile
     * @throws IOException
     */
    public PropertiesFileBasedPropertySource(ExternalFile externalFile) throws IOException {
        this.externalFile = externalFile;
        this.properties = new Properties();
        loadProperties();
    }

    @Override
    public String getDescription() {
        return externalFile.getPath();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }

    @Override
    public Optional<String> getValue(String name) {
        if (properties.containsKey(name)) {
            return Optional.of(properties.getProperty(name));
        }
        return Optional.absent();
    }

    @Override
    public boolean isReloadable() { return true; }

    @Override
    public void reload() throws IOException {
        loadProperties();
        // TODO: Fire reloaded event
    }

    @Override
    public void addListener(PropertySourceListener listener) {
        // TODO: Add listener
    }

    @Override
    public void removeListener(PropertySourceListener listener) {
        // TODO: Remove listener
    }

    private void loadProperties() throws IOException {
        InputStream is = externalFile.getInputStream();
        try {
            properties.load(externalFile.getInputStream());
        } finally {
            is.close();
        }
    }
}
