package dev.openfeature.sdk;

/**
 * @deprecated Use {@link dev.openfeature.api.ErrorCode} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.ErrorCode;
 * ErrorCode code = ErrorCode.PROVIDER_NOT_READY;
 *
 * // After
 * import dev.openfeature.api.ErrorCode;
 * ErrorCode code = ErrorCode.PROVIDER_NOT_READY;
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public final class ErrorCode {

    /** @deprecated Use {@link dev.openfeature.api.ErrorCode#PROVIDER_NOT_READY} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final dev.openfeature.api.ErrorCode PROVIDER_NOT_READY = dev.openfeature.api.ErrorCode.PROVIDER_NOT_READY;

    /** @deprecated Use {@link dev.openfeature.api.ErrorCode#FLAG_NOT_FOUND} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final dev.openfeature.api.ErrorCode FLAG_NOT_FOUND = dev.openfeature.api.ErrorCode.FLAG_NOT_FOUND;

    /** @deprecated Use {@link dev.openfeature.api.ErrorCode#PARSE_ERROR} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final dev.openfeature.api.ErrorCode PARSE_ERROR = dev.openfeature.api.ErrorCode.PARSE_ERROR;

    /** @deprecated Use {@link dev.openfeature.api.ErrorCode#TYPE_MISMATCH} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final dev.openfeature.api.ErrorCode TYPE_MISMATCH = dev.openfeature.api.ErrorCode.TYPE_MISMATCH;

    /** @deprecated Use {@link dev.openfeature.api.ErrorCode#TARGETING_KEY_MISSING} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final dev.openfeature.api.ErrorCode TARGETING_KEY_MISSING = dev.openfeature.api.ErrorCode.TARGETING_KEY_MISSING;

    /** @deprecated Use {@link dev.openfeature.api.ErrorCode#INVALID_CONTEXT} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final dev.openfeature.api.ErrorCode INVALID_CONTEXT = dev.openfeature.api.ErrorCode.INVALID_CONTEXT;

    /** @deprecated Use {@link dev.openfeature.api.ErrorCode#GENERAL} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final dev.openfeature.api.ErrorCode GENERAL = dev.openfeature.api.ErrorCode.GENERAL;

    private ErrorCode() {
        // Utility class
    }

    /**
     * Convert this deprecated enum value to the new API enum.
     * @param errorCode The deprecated error code
     * @return The equivalent value in the new API
     */
    public static dev.openfeature.api.ErrorCode toApiType(dev.openfeature.api.ErrorCode errorCode) {
        return errorCode; // They're the same instances, just re-exported
    }

    /**
     * Convert from the new API enum to this deprecated enum.
     * @param apiErrorCode The new API enum value
     * @return The equivalent deprecated enum value (same instance)
     */
    public static dev.openfeature.api.ErrorCode fromApiType(dev.openfeature.api.ErrorCode apiErrorCode) {
        return apiErrorCode; // They're the same instances, just re-exported
    }
}
