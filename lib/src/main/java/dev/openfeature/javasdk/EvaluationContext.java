package dev.openfeature.javasdk;

import lombok.Getter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EvaluationContext {
    @Getter private final String targetingKey;
    private final Map<String, Integer> integerAttributes;
    private final Map<String, String> stringAttributes;


    private enum KNOWN_KEYS {
        EMAIL,
        FIRST_NAME,
        LAST_NAME,
        NAME,
        IP,
        TZ,
        LOCALE,
        COUNTRY_CODE,
        ENVIRONMENT,
        APPLICATION,
        VERSION,
        TIMESTAMP,
    }

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

    public ZonedDateTime getDatetimeAttribute(String key) {
        String attr = this.stringAttributes.get(key);
        if (attr == null) {
            return null;
        }
        return ZonedDateTime.parse(attr, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    public String getEmail() {
        return this.stringAttributes.get(KNOWN_KEYS.EMAIL.toString());
    }

    public String getFirstName() {
        return this.stringAttributes.get(KNOWN_KEYS.FIRST_NAME.toString());
    }

    public String getLastName() {
        return this.stringAttributes.get(KNOWN_KEYS.LAST_NAME.toString());
    }

    public String getName() {
        return this.stringAttributes.get(KNOWN_KEYS.NAME.toString());
    }

    public String getIp() {
        return this.stringAttributes.get(KNOWN_KEYS.IP.toString());
    }

    public String getTz() {
        return this.stringAttributes.get(KNOWN_KEYS.TZ.toString());
    }

    public String getLocale() {
        return this.stringAttributes.get(KNOWN_KEYS.LOCALE.toString());
    }

    public String getCountryCode() {
        return this.stringAttributes.get(KNOWN_KEYS.COUNTRY_CODE.toString());
    }

    public String getEnvironment() {
        return this.stringAttributes.get(KNOWN_KEYS.ENVIRONMENT.toString());
    }

    public String getApplication() {
        return this.stringAttributes.get(KNOWN_KEYS.APPLICATION.toString());
    }

    public String getVersion() {
        return this.stringAttributes.get(KNOWN_KEYS.VERSION.toString());
    }

    public ZonedDateTime getTimestamp() {
        return getDatetimeAttribute(KNOWN_KEYS.TIMESTAMP.toString());
    }

    public void setEmail(String email) {
        this.stringAttributes.put(KNOWN_KEYS.EMAIL.toString(), email);
    }

    public void setFirstName(String firstname) {
        this.stringAttributes.put(KNOWN_KEYS.FIRST_NAME.toString(), firstname);
    }

    public void setLastName(String lastname) {
        this.stringAttributes.put(KNOWN_KEYS.LAST_NAME.toString(), lastname);
    }

    public void setName(String name) {
        this.stringAttributes.put(KNOWN_KEYS.NAME.toString(), name);
    }

    public void setIp(String ip) {
        this.stringAttributes.put(KNOWN_KEYS.IP.toString(), ip);
    }

    public void setTz(String tz) {
        this.stringAttributes.put(KNOWN_KEYS.TZ.toString(), tz);
    }

    public void setLocale(String locale) {
        this.stringAttributes.put(KNOWN_KEYS.LOCALE.toString(), locale);
    }

    public void setCountryCode(String countryCode) {
        this.stringAttributes.put(KNOWN_KEYS.COUNTRY_CODE.toString(), countryCode);
    }

    public void setEnvironment(String environment) {
        this.stringAttributes.put(KNOWN_KEYS.ENVIRONMENT.toString(), environment);
    }

    public void setApplication(String application) {
        this.stringAttributes.put(KNOWN_KEYS.APPLICATION.toString(), application);
    }

    public void setVersion(String version) {
        this.stringAttributes.put(KNOWN_KEYS.VERSION.toString(), version);
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        addDatetimeAttribute(KNOWN_KEYS.TIMESTAMP.toString(), timestamp);
    }

    /**
     * Merges two EvaluationContext objects with the second overriding the first in case of conflict.
     */
    public static EvaluationContext merge(EvaluationContext ctx1, EvaluationContext ctx2) {
        // TODO(abrahms): Actually implement this when we know what the fields of EC are.
        return ctx1;
    }
}
