package dev.openfeature.api;

import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * ImmutableTrackingEventDetails represents data pertinent to a particular tracking event.
 */
public class ImmutableTrackingEventDetails implements TrackingEventDetails {

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

    @Override
    public Value getValue(String key) {
        return structure.getValue(key);
    }

    // Delegated methods from ImmutableStructure
    @Override
    public boolean isEmpty() {
        return structure.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return structure.keySet();
    }

    @Override
    public Map<String, Value> asMap() {
        return structure.asMap();
    }

    @Override
    public Map<String, Value> asUnmodifiableMap() {
        return structure.asUnmodifiableMap();
    }

    @Override
    public Map<String, Object> asObjectMap() {
        return structure.asObjectMap();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ImmutableTrackingEventDetails that = (ImmutableTrackingEventDetails) obj;
        return Objects.equals(structure, that.structure) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure, value);
    }

    @Override
    public String toString() {
        return "ImmutableTrackingEventDetails{" + "structure=" + structure + ", value=" + value + '}';
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
