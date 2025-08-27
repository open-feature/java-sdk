package dev.openfeature.api;

import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * MutableTrackingEventDetails represents data pertinent to a particular tracking event.
 */
public class MutableTrackingEventDetails implements TrackingEventDetails {

    private final Number value;
    private final MutableStructure structure;

    public MutableTrackingEventDetails() {
        this.value = null;
        this.structure = new MutableStructure();
    }

    public MutableTrackingEventDetails(final Number value) {
        this.value = value;
        this.structure = new MutableStructure();
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

    // override @Delegate methods so that we can use "add" methods and still return MutableTrackingEventDetails,
    // not Structure
    public MutableTrackingEventDetails add(String key, Boolean value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableTrackingEventDetails add(String key, String value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableTrackingEventDetails add(String key, Integer value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableTrackingEventDetails add(String key, Double value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableTrackingEventDetails add(String key, Instant value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableTrackingEventDetails add(String key, Structure value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableTrackingEventDetails add(String key, List<Value> value) {
        this.structure.add(key, value);
        return this;
    }

    public MutableTrackingEventDetails add(String key, Value value) {
        this.structure.add(key, value);
        return this;
    }

    // Delegated methods from MutableStructure
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
        MutableTrackingEventDetails that = (MutableTrackingEventDetails) obj;
        return Objects.equals(value, that.value) && Objects.equals(structure, that.structure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, structure);
    }

    @Override
    public String toString() {
        return "MutableTrackingEventDetails{" + "value=" + value + ", structure=" + structure + '}';
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
