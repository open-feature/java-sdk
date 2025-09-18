package dev.openfeature.api;

import java.util.Map;
import java.util.Optional;

/**
 * Data pertinent to a particular tracking event.
 */
public interface TrackingEventDetails extends Structure {

    TrackingEventDetails EMPTY = immutableBuilder().build();

    /**
     * Returns the optional numeric tracking value.
     */
    Optional<Number> getValue();

    static ImmutableTrackingEventDetailsBuilder immutableBuilder() {
        return new ImmutableTrackingEventDetails.Builder();
    }

    static TrackingEventDetails immutableOf(Number value) {
        return immutableOf(value, null);
    }

    static TrackingEventDetails immutableOf(Number value, Map<String, Value> attributes) {
        return new ImmutableTrackingEventDetails(value, attributes);
    }
}
