package dev.openfeature.api;

import java.util.Map;

/**
 * Builder class for creating instances of ImmutableTrackingEventDetails.
 */
public interface ImmutableTrackingEventDetailsBuilder {
    ImmutableTrackingEventDetailsBuilder value(Number value);

    ImmutableTrackingEventDetailsBuilder attributes(Map<String, Value> attributes);

    ImmutableTrackingEventDetailsBuilder add(String key, String value);

    ImmutableTrackingEventDetailsBuilder add(String key, Integer value);

    ImmutableTrackingEventDetailsBuilder add(String key, Long value);

    ImmutableTrackingEventDetailsBuilder add(String key, Float value);

    ImmutableTrackingEventDetailsBuilder add(String key, Double value);

    ImmutableTrackingEventDetailsBuilder add(String key, Boolean value);

    ImmutableTrackingEventDetailsBuilder add(String key, Structure value);

    ImmutableTrackingEventDetailsBuilder add(String key, Value value);

    TrackingEventDetails build();
}
