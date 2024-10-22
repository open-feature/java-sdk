package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

@SuppressWarnings({ "PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType" })
abstract class AbstractStructure implements Structure {

    protected final Map<String, Value> attributes;

    @Override
    public boolean isEmpty() {
        return attributes == null || attributes.size() == 0;
    }

    AbstractStructure() {
        this.attributes = new HashMap<>();
    }

    AbstractStructure(Map<String, Value> attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns an unmodifiable representation of the internal attribute map.
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
        return attributes
                .entrySet()
                .stream()
                // custom collector, workaround for Collectors.toMap in JDK8
                // https://bugs.openjdk.org/browse/JDK-8148463
                .collect(HashMap::new,
                        (accumulated, entry) -> accumulated.put(entry.getKey(), convertValue(entry.getValue())),
                        HashMap::putAll);
    }

}
