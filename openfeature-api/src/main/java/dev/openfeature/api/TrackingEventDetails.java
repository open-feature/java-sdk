package dev.openfeature.api;

import java.util.Optional;

/**
 * Data pertinent to a particular tracking event.
 */
public interface TrackingEventDetails extends Structure {

    /**
     * Returns the optional numeric tracking value.
     */
    Optional<Number> getValue();
}
