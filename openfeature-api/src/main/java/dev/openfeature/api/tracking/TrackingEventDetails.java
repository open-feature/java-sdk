package dev.openfeature.api.tracking;

import dev.openfeature.api.types.Structure;
import dev.openfeature.api.types.Value;
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
