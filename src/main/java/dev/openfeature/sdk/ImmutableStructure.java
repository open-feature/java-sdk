package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
@SuppressWarnings({ "PMD.BeanMembersShouldSerialize", "checkstyle:MissingJavadocType" })
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
        super(copyAttributes(attributes));
    }

    @Override
    public Set<String> keySet() {
        return new HashSet<>(this.getAttributes().keySet());
    }

    // getters
    @Override
    public Value getValue(String key) {
        Value value = getAttributes().get(key);
        return value != null ? value.clone() : null;
    }

    /**
     * Get all values.
     *
     * @return all attributes on the structure
     */
    @Override
    public Map<String, Value> asMap() {
        return copyAttributes(getAttributes());
    }

    private static Map<String, Value> copyAttributes(Map<String, Value> in) {
        Map<String, Value> copy = new HashMap<>();
        for (Entry<String, Value> entry : in.entrySet()) {
            copy.put(entry.getKey(),
                    Optional.ofNullable(entry.getValue()).map((Value val) -> val.clone()).orElse(null));
        }
        return copy;
    }

}
