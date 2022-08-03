package dev.openfeature.javasdk;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    public <T> void addStructureAttribute(String key, T value) {
        jsonAttributes.put(key, objMapper.writeValueAsString(value));
    }

    @SneakyThrows
    public <T> T getStructureAttribute(String key, Class<T> klass) {
        String val = jsonAttributes.get(key);
        return objMapper.readValue(val, klass);
    }

    public Map<String, String> getStructureAttributes() {
        return new HashMap<>(jsonAttributes);
    }

    public void addStringAttribute(String key, String value) {
        stringAttributes.put(key, value);
    }

    public String getStringAttribute(String key) {
        return stringAttributes.get(key);
    }

    public Map<String, String> getStringAttributes() {
        return new HashMap<>(stringAttributes);
    }

    public void addIntegerAttribute(String key, Integer value) {
        integerAttributes.put(key, value);
    }

    public Integer getIntegerAttribute(String key) {
        return integerAttributes.get(key);
    }

    public Map<String, Integer> getIntegerAttributes() {
        return new HashMap<>(integerAttributes);
    }

    public Boolean getBooleanAttribute(String key) {
        return booleanAttributes.get(key);
    }

    public void addBooleanAttribute(String key, Boolean b) {
        booleanAttributes.put(key, b);
    }

    public Map<String, Boolean> getBooleanAttributes() {
        return new HashMap<>(booleanAttributes);
    }

    public void addDatetimeAttribute(String key, ZonedDateTime value) {
        this.stringAttributes.put(key, value.format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
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
}
