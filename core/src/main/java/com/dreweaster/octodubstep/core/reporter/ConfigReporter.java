package com.dreweaster.octodubstep.core.reporter;

import com.dreweaster.octodubstep.core.ConfigPropertyValue;

/**
 */
public interface ConfigReporter {

    void propertyLoaded(ConfigPropertyValue<?> propertyValue);
}
