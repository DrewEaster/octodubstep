package com.dreweaster.octodubstep.core;

import com.google.common.base.Optional;
import org.joda.time.DateTime;

/**
 */
public interface Value<V> {

    Optional<V> currentValue();

    Optional<DateTime> lastAccessed();

    PropertyMetadata metadata();
}
