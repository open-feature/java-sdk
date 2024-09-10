package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * {@link ImmutableStructure} represents a potentially nested object type which
 * is used to represent
 * structured data.
 * The ImmutableStructure is a Structure implementation which is threadsafe, and
 * whose attributes can
 * not be modified after instantiation. All references are clones.
 */
@ToString
@EqualsAndHashCode
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType"})
public final class ImmutableStructure extends AbstractStructure {

    /**
     * create an immutable structure with the empty attributes.
     */
    public ImmutableStructure() {
        super();
    }

    /**
     * create immutable structure with the given attributes.
     *
     * @param attributes attributes.
     */
    public ImmutableStructure(Map<String, Value> attributes) {
        super(new HashMap<>(attributes.entrySet()
                .stream()
                .collect(HashMap::new,
                        (accumulated, entry) -> accumulated.put(entry.getKey(),
                                Optional.ofNullable(entry.getValue())
                                        .map(Value::clone)
                                        .orElse(null)),
                        HashMap::putAll)));
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>(this.attributes.keySet());
    }

    // getters
    @Override
    public Value getValue(String key) {
        Value value = this.attributes.get(key);
        return value != null ? value.clone() : null;
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
                .collect(HashMap::new,
                        (accumulated, entry) -> accumulated.put(entry.getKey(),
                                Optional.ofNullable(entry.getValue())
                                        .map(Value::clone)
                                        .orElse(null)),
                        HashMap::putAll);
    }
}
