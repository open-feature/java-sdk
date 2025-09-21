package dev.openfeature.sdk;

/**
 * @deprecated Use {@link dev.openfeature.api.Reason} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.Reason;
 * String reason = Reason.DEFAULT;
 *
 * // After
 * import dev.openfeature.api.Reason;
 * String reason = Reason.DEFAULT;
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public final class Reason {

    /** @deprecated Use {@link dev.openfeature.api.Reason#STATIC} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String STATIC = dev.openfeature.api.Reason.STATIC;

    /** @deprecated Use {@link dev.openfeature.api.Reason#DEFAULT} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String DEFAULT = dev.openfeature.api.Reason.DEFAULT;

    /** @deprecated Use {@link dev.openfeature.api.Reason#TARGETING_MATCH} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String TARGETING_MATCH = dev.openfeature.api.Reason.TARGETING_MATCH;

    /** @deprecated Use {@link dev.openfeature.api.Reason#SPLIT} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String SPLIT = dev.openfeature.api.Reason.SPLIT;

    /** @deprecated Use {@link dev.openfeature.api.Reason#CACHED} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String CACHED = dev.openfeature.api.Reason.CACHED;

    /** @deprecated Use {@link dev.openfeature.api.Reason#DISABLED} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String DISABLED = dev.openfeature.api.Reason.DISABLED;

    /** @deprecated Use {@link dev.openfeature.api.Reason#UNKNOWN} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String UNKNOWN = dev.openfeature.api.Reason.UNKNOWN;

    /** @deprecated Use {@link dev.openfeature.api.Reason#STALE} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String STALE = dev.openfeature.api.Reason.STALE;

    /** @deprecated Use {@link dev.openfeature.api.Reason#ERROR} */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final String ERROR = dev.openfeature.api.Reason.ERROR;

    private Reason() {
        // Utility class
    }
}