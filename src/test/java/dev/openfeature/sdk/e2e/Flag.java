package dev.openfeature.sdk.e2e;

import dev.openfeature.sdk.ImmutableMetadata;

public class Flag {
    public final String name;
    public final Object defaultValue;
    public final String type;
    public final ImmutableMetadata flagMetadata;

    public Flag(String type, String name, Object defaultValue) {
        this(type, name, defaultValue, ImmutableMetadata.EMPTY);
    }

    public Flag(String type, String name, Object defaultValue, ImmutableMetadata flagMetadata) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;
        this.flagMetadata = flagMetadata;
    }
}
