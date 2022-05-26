package dev.openfeature.javasdk;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ToString @EqualsAndHashCode
public class EvaluationContext {
    @Setter @Getter private String targetingKey;
    private final Map<String, Integer> integerAttributes;
    private final Map<String, String> stringAttributes;

    EvaluationContext() {
        this.targetingKey = "";
        this.integerAttributes = new HashMap<>();
        this.stringAttributes = new HashMap<>();
    }

    public void addStringAttribute(String key, String value) {
        stringAttributes.put(key, value);
    }

    public String getStringAttribute(String key) {
        return stringAttributes.get(key);
    }

    public void addIntegerAttribute(String key, Integer value) {
        integerAttributes.put(key, value);
    }

    public Integer getIntegerAttribute(String key) {
        return integerAttributes.get(key);
    }

    public Boolean getBooleanAttribute(String key) {
        return Boolean.valueOf(stringAttributes.get(key));
    }

    public void addBooleanAttribute(String key, Boolean b) {
        stringAttributes.put(key, b.toString());
    }

    public void addDatetimeAttribute(String key, ZonedDateTime value) {
        this.stringAttributes.put(key, value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
    }

    // TODO: addStructure or similar.

    public ZonedDateTime getDatetimeAttribute(String key) {
        String attr = this.stringAttributes.get(key);
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
        for (Map.Entry<String, Integer> e : ctx1.integerAttributes.entrySet()) {
            ec.addIntegerAttribute(e.getKey(), e.getValue());
        }

        for (Map.Entry<String, Integer> e : ctx2.integerAttributes.entrySet()) {
            ec.addIntegerAttribute(e.getKey(), e.getValue());
        }

        for (Map.Entry<String, String> e : ctx1.stringAttributes.entrySet()) {
            ec.addStringAttribute(e.getKey(), e.getValue());
        }

        for (Map.Entry<String, String> e : ctx2.stringAttributes.entrySet()) {
            ec.addStringAttribute(e.getKey(), e.getValue());
        }
        if (ctx1.getTargetingKey() != null) {
            ec.setTargetingKey(ctx1.getTargetingKey());
        }

        if (ctx2.getTargetingKey() != null) {
            ec.setTargetingKey(ctx2.getTargetingKey());
        }

        return ec;
    }
}
