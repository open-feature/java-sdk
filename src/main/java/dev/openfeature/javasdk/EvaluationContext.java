package dev.openfeature.javasdk;

import dev.openfeature.javasdk.internal.Pair;
import lombok.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ToString @EqualsAndHashCode
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class EvaluationContext {
    @Setter @Getter private String targetingKey;
    private final Map<String, dev.openfeature.javasdk.internal.Pair<FlagValueType, Object>> attributes;

    public EvaluationContext() {
        this.targetingKey = "";
        this.attributes = new HashMap<>();
    }

    // TODO Not sure if I should have sneakythrows or checked exceptions here..
    @SneakyThrows
    public <T> void addStructureAttribute(String key, T value) {
        attributes.put(key, new Pair<>(FlagValueType.OBJECT, value));
    }

    @SneakyThrows
    public <T> T getStructureAttribute(String key) {
        return getAttributeByType(key, FlagValueType.OBJECT);
    }

    public Map<String, String> getStructureAttributes() {
        return getAttributesByType(FlagValueType.OBJECT);
    }

    public void addStringAttribute(String key, String value) {
        attributes.put(key, new Pair<>(FlagValueType.STRING, value));
    }

    public String getStringAttribute(String key) {
        return getAttributeByType(key, FlagValueType.STRING);
    }

    private <T> Map<String, T> getAttributesByType(FlagValueType type) {
        HashMap<String, T> hm = new HashMap<>();
        for (Map.Entry<String, Pair<FlagValueType, Object>> entry : attributes.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue().getFirst() == type) {
                hm.put(key, (T) entry.getValue().getSecond());
            }
        }
        return hm;
    }

    private <T> T getAttributeByType(String key, FlagValueType type) {
        Pair<FlagValueType, Object> val = attributes.get(key);
        if (val.getFirst() == type) {
            return (T) val.getSecond();
        }
        return null;
    }

    public Map<String, String> getStringAttributes() {
        return getAttributesByType(FlagValueType.STRING);
    }

    public void addIntegerAttribute(String key, Integer value) {
        attributes.put(key, new Pair<>(FlagValueType.INTEGER, value));
    }

    public Integer getIntegerAttribute(String key) {
        return getAttributeByType(key, FlagValueType.INTEGER);
    }

    public Map<String, Integer> getIntegerAttributes() {
        return getAttributesByType(FlagValueType.INTEGER);
    }

    public Boolean getBooleanAttribute(String key) {
        return getAttributeByType(key, FlagValueType.BOOLEAN);
    }

    public void addBooleanAttribute(String key, Boolean b) {
        attributes.put(key, new Pair<>(FlagValueType.BOOLEAN, b));
    }

    public Map<String, Boolean> getBooleanAttributes() {
        return getAttributesByType(FlagValueType.BOOLEAN);
    }

    public void addDatetimeAttribute(String key, ZonedDateTime value) {
        attributes.put(key, new Pair<>(FlagValueType.STRING, value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)));
    }

    public ZonedDateTime getDatetimeAttribute(String key) {
        String attr = getAttributeByType(key, FlagValueType.STRING);
        if (attr == null) {
            return null;
        }
        return ZonedDateTime.parse(attr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * Merges two EvaluationContext objects with the second overriding the first in case of conflict.
     */
    public static EvaluationContext merge(EvaluationContext ctx1, EvaluationContext ctx2) {
        EvaluationContext ec = new EvaluationContext();
        ec.attributes.putAll(ctx1.attributes);
        ec.attributes.putAll(ctx2.attributes);

        if (ctx1.getTargetingKey() != null) {
            ec.setTargetingKey(ctx1.getTargetingKey());
        }

        if (ctx2.getTargetingKey() != null) {
            ec.setTargetingKey(ctx2.getTargetingKey());
        }

        return ec;
    }
}
