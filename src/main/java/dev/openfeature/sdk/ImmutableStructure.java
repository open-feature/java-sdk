package dev.openfeature.sdk;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ImmutableStructure} represents a potentially nested object type which is used to represent
 * structured data.
 * The ImmutableStructure is a Structure implementation which is threadsafe, and whose attributes can
 * not be modified after instantiation.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType"})
public final class ImmutableStructure implements Structure {

    private final Map<String, Value> attributes;

    /**
     * create an immutable structure with the empty attributes.
     */
    public ImmutableStructure() {
        this(new HashMap<>());
    }

    /**
     * create immutable structure with the given attributes.
     *
     * @param attributes attributes.
     */
    public ImmutableStructure(Map<String, Value> attributes) {
        Map<String, Value> copy = attributes.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().clone()));
        this.attributes = new HashMap<>(copy);
    }

    @Override
    public Set<String> keySet() {
        return this.attributes.keySet();
    }

    // getters
    @Override
    public Value getValue(String key) {
        Value value = this.attributes.get(key);
        return value.clone();
    }

    /**
     * Get all values.
     *
     * @return all attributes on the structure
     */
    @Override
    public Map<String, Value> asMap() {
        return attributes
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> getValue(e.getKey())
                ));
    }

    /**
     * Get all values, with primitives types.
     *
     * @return all attributes on the structure into a Map
     */
    @Override
    public Map<String, Object> asObjectMap() {
        return attributes
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> convertValue(getValue(e.getKey()))
                ));
    }
}
