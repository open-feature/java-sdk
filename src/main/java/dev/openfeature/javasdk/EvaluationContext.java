package dev.openfeature.javasdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.stream.Collectors;
import lombok.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ToString @EqualsAndHashCode
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class EvaluationContext {
    @EqualsAndHashCode.Exclude private final ObjectMapper objMapper;
    @Setter @Getter private String targetingKey;
    private final Map<String, Integer> integerAttributes;
    private final Map<String, String> stringAttributes;
    private final Map<String, Boolean> booleanAttributes;
    final Map<String, String> jsonAttributes;

    public EvaluationContext() {
        objMapper = new ObjectMapper();
        this.targetingKey = "";
        this.integerAttributes = new HashMap<>();
        this.stringAttributes = new HashMap<>();
        booleanAttributes = new HashMap<>();
        jsonAttributes = new HashMap<>();
    }

    // TODO Not sure if I should have sneakythrows or checked exceptions here..
    @SneakyThrows
    public <T> EvaluationContext withStructureAttribute(String key, T value) {
        jsonAttributes.put(key, objMapper.writeValueAsString(value));
        return this;
    }

    @SneakyThrows
    public <T> T getStructureAttribute(String key, Class<T> klass) {
        String val = jsonAttributes.get(key);
        return objMapper.readValue(val, klass);
    }

    public EvaluationContext withStringAttribute(String key, String value) {
        stringAttributes.put(key, value);
        return this;
    }

    public String getStringAttribute(String key) {
        return stringAttributes.get(key);
    }

    public EvaluationContext withIntegerAttribute(String key, Integer value) {
        integerAttributes.put(key, value);
        return this;
    }

    public Integer getIntegerAttribute(String key) {
        return integerAttributes.get(key);
    }

    public Boolean getBooleanAttribute(String key) {
        return booleanAttributes.get(key);
    }

    public EvaluationContext withBooleanAttribute(String key, Boolean b) {
        booleanAttributes.put(key, b);
        return this;
    }

    public EvaluationContext withDatetimeAttribute(String key, ZonedDateTime value) {
        this.stringAttributes.put(key, value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        return this;
    }

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

        ec.stringAttributes.putAll(ctx1.stringAttributes);
        ec.stringAttributes.putAll(ctx2.stringAttributes);

        ec.integerAttributes.putAll(ctx1.integerAttributes);
        ec.integerAttributes.putAll(ctx2.integerAttributes);

        ec.booleanAttributes.putAll(ctx1.booleanAttributes);
        ec.booleanAttributes.putAll(ctx2.booleanAttributes);

        ec.jsonAttributes.putAll(ctx1.jsonAttributes);
        ec.jsonAttributes.putAll(ctx2.jsonAttributes);

        if (ctx1.getTargetingKey() != null) {
            ec.setTargetingKey(ctx1.getTargetingKey());
        }

        if (ctx2.getTargetingKey() != null) {
            ec.setTargetingKey(ctx2.getTargetingKey());
        }

        return ec;
    }

    public EvaluationContext fromMap(Map<String, Object> map) {
        EvaluationContext context = new EvaluationContext();
        context.integerAttributes.putAll(
                map.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() instanceof Integer)
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> (Integer) e.getValue()))
        );
        context.stringAttributes.putAll(
            map.entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof String)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String)e.getValue()))
        );
        context.booleanAttributes.putAll(
            map.entrySet()
                .stream()
                .filter(entry -> entry.getValue() instanceof Boolean)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Boolean)e.getValue()))
        );
//        Hmmm, this won't work as we already have a string type.
//        I would recommend changing the type to Map<String, JsonNode> from Jackson
//        context.jsonAttributes.putAll(
//            map.entrySet()
//                .stream()
//                .filter(entry -> entry.getValue() instanceof String)
//                .collect(Collectors.toMap(e -> e.getKey(), e -> (String)e.getValue()))
//        );

        return null;
    }

    /**
     * Converts the Evaluation Context into a standard {@link Map}
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        // 🤔 This will fail if two different maps have the same key.
        map.putAll(integerAttributes);
        map.putAll(stringAttributes);
        map.putAll(booleanAttributes);
        map.putAll(jsonAttributes);
        return ImmutableMap.copyOf(map);
    }
}
