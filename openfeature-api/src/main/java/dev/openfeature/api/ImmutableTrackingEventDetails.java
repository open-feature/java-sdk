package dev.openfeature.api;

import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.experimental.Delegate;

/**
 * ImmutableTrackingEventDetails represents data pertinent to a particular tracking event.
 */
public class ImmutableTrackingEventDetails implements TrackingEventDetails {

    @Delegate(excludes = DelegateExclusions.class)
    private final ImmutableStructure structure;

    private final Number value;

    public ImmutableTrackingEventDetails() {
        this.value = null;
        this.structure = new ImmutableStructure();
    }

    public ImmutableTrackingEventDetails(final Number value) {
        this.value = value;
        this.structure = new ImmutableStructure();
    }

    public ImmutableTrackingEventDetails(final Number value, final Map<String, Value> attributes) {
        this.value = value;
        this.structure = new ImmutableStructure(attributes);
    }

    /**
     * Returns the optional tracking value.
     */
    public Optional<Number> getValue() {
        return Optional.ofNullable(value);
    }

    @SuppressWarnings("all")
    private static class DelegateExclusions {
        @ExcludeFromGeneratedCoverageReport
        public <T extends Structure> Map<String, Value> merge(
                Function<Map<String, Value>, Structure> newStructure,
                Map<String, Value> base,
                Map<String, Value> overriding) {
            return null;
        }
    }
}
