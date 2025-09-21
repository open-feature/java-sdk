package dev.openfeature.sdk;

/**
 * @deprecated Use {@link dev.openfeature.api.FlagValueType} instead.
 * This enum will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.FlagValueType;
 * FlagValueType type = FlagValueType.BOOLEAN;
 *
 * // After
 * import dev.openfeature.api.FlagValueType;
 * FlagValueType type = FlagValueType.BOOLEAN;
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public enum FlagValueType {
    /** @deprecated Use {@link dev.openfeature.api.FlagValueType#BOOLEAN} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    BOOLEAN,

    /** @deprecated Use {@link dev.openfeature.api.FlagValueType#STRING} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    STRING,

    /** @deprecated Use {@link dev.openfeature.api.FlagValueType#INTEGER} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    INTEGER,

    /** @deprecated Use {@link dev.openfeature.api.FlagValueType#DOUBLE} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    DOUBLE,

    /** @deprecated Use {@link dev.openfeature.api.FlagValueType#OBJECT} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    OBJECT;

    /**
     * Convert this deprecated enum value to the new API enum.
     * @return The equivalent value in the new API
     */
    public dev.openfeature.api.FlagValueType toApiType() {
        switch (this) {
            case BOOLEAN: return dev.openfeature.api.FlagValueType.BOOLEAN;
            case STRING: return dev.openfeature.api.FlagValueType.STRING;
            case INTEGER: return dev.openfeature.api.FlagValueType.INTEGER;
            case DOUBLE: return dev.openfeature.api.FlagValueType.DOUBLE;
            case OBJECT: return dev.openfeature.api.FlagValueType.OBJECT;
            default: throw new IllegalStateException("Unknown type: " + this);
        }
    }

    /**
     * Convert from the new API enum to this deprecated enum.
     * @param apiType The new API enum value
     * @return The equivalent deprecated enum value
     */
    public static FlagValueType fromApiType(dev.openfeature.api.FlagValueType apiType) {
        switch (apiType) {
            case BOOLEAN: return BOOLEAN;
            case STRING: return STRING;
            case INTEGER: return INTEGER;
            case DOUBLE: return DOUBLE;
            case OBJECT: return OBJECT;
            default: throw new IllegalArgumentException("Unknown API type: " + apiType);
        }
    }
}