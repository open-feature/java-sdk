package dev.openfeature.javasdk;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dev.openfeature.javasdk.internal.Pair;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class Structure {

    protected final Map<String, Pair<FlagValueType, Object>> attributes;

    public Structure() {
        this.attributes = new HashMap<>();
    }

    public Set<String> keySet() {
        return this.attributes.keySet();
    }

    // getters
    public Boolean getBooleanAttribute(String key) {
        return getAttributeByType(key, FlagValueType.BOOLEAN);
    }

    public String getStringAttribute(String key) {
        return getAttributeByType(key, FlagValueType.STRING);
    }

    public Integer getIntegerAttribute(String key) {
        return getAttributeByType(key, FlagValueType.INTEGER);
    }

    public Double getDoubleAttribute(String key) {
        return getAttributeByType(key, FlagValueType.DOUBLE);
    }

    public <T> List<T> getArrayAttribute(String key) {
        return getAttributeByType(key, FlagValueType.ARRAY);
    }

    public Structure getStructureAttribute(String key) {
        return getAttributeByType(key, FlagValueType.OBJECT);
    }

    /**
     * Fetch date-time relevant key.
     * 
     * @param key feature key
     * @return date time object.
     * @throws java.time.format.DateTimeParseException if it's not a datetime
     */
    public ZonedDateTime getDatetimeAttribute(String key) {
        String attr = getAttributeByType(key, FlagValueType.STRING);
        if (attr == null) {
            return null;
        }
        return ZonedDateTime.parse(attr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    // adders
    public Structure add(String key, Boolean value) {
        attributes.put(key, new Pair<>(FlagValueType.BOOLEAN, value));
        return this;
    }

    public Structure add(String key, String value) {
        attributes.put(key, new Pair<>(FlagValueType.STRING, value));
        return this;
    }

    public Structure add(String key, Integer value) {
        attributes.put(key, new Pair<>(FlagValueType.INTEGER, value));
        return this;
    }

    public Structure add(String key, Double value) {
        attributes.put(key, new Pair<>(FlagValueType.DOUBLE, value));
        return this;
    }

    public Structure add(String key, ZonedDateTime value) {
        attributes.put(key, new Pair<>(FlagValueType.STRING, value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));
        return this;
    }

    public Structure add(String key, Structure value) {
        attributes.put(key, new Pair<>(FlagValueType.OBJECT, value));
        return this;
    }

    public <T> Structure add(String key, List<T> value) {
        attributes.put(key, new Pair<>(FlagValueType.ARRAY, value));
        return this;
    }

    /**
     * Get all attributes, regardless of type.
     * 
     * @return all attributes on the structure
     */
    public Map<String, Object> getAllAttributes() {
        HashMap<String, Object> hm = new HashMap<>();
        for (Map.Entry<String, Pair<FlagValueType, Object>> entry : attributes.entrySet()) {
            hm.put(entry.getKey(), entry.getValue().getInnerValue());
        }
        return hm;
    }

    private <T> T getAttributeByType(String key, FlagValueType type) {
        Pair<FlagValueType, Object> val = attributes.get(key);
        if (val == null) {
            return null;
        }
        if (val.getType() == type) {
            return (T) val.getInnerValue();
        }
        return null;
    }
}
