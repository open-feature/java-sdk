package dev.openfeature.sdk.e2e;

public class Flag {
    public String name;
    public Object defaultValue;
    public String type;

    public Flag(String type, String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;
    }
}
