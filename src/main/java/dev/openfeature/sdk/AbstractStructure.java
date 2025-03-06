package dev.openfeature.sdk;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType"})
abstract class AbstractStructure implements Structure {

    protected final Map<String, Value> attributes;

    @Override
    public boolean isEmpty() {
        return attributes == null || attributes.isEmpty();
    }

    AbstractStructure() {
        this.attributes = new HashMap<>();
    }

    AbstractStructure(Map<String, Value> attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns an unmodifiable representation of the internal attribute map.
     *
     * @return immutable map
     */
    public Map<String, Value> asUnmodifiableMap() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * Get all values as their underlying primitives types.
     *
     * @return all attributes on the structure into a Map
     */
    @Override
    public Map<String, Object> asObjectMap() {
        return attributes.entrySet().stream()
                // custom collector, workaround for Collectors.toMap in JDK8
                // https://bugs.openjdk.org/browse/JDK-8148463
                .collect(
                        HashMap::new,
                        (accumulated, entry) -> accumulated.put(entry.getKey(), convertValue(entry.getValue())),
                        HashMap::putAll);
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AbstractStructure)) {
            return false;
        }
        final AbstractStructure other = (AbstractStructure) o;
        if (other.attributes == attributes) {
            return true;
        }
        if (attributes == null || other.attributes == null) {
            return false;
        }
        if (other.attributes.size() != attributes.size()) {
            return false;
        }

        for (Map.Entry<String, Value> thisEntry : attributes.entrySet()) {
            Value thisValue = thisEntry.getValue();
            Value otherValue = other.attributes.get(thisEntry.getKey());
            if (thisValue == null && otherValue == null) {
                continue;
            }
            if (thisValue == null || otherValue == null) {
                return false;
            }
            if (!thisValue.equals(otherValue)) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        if (attributes == null) {
            return 0;
        }
        return attributes.hashCode();
    }
}
