package dev.openfeature.sdk;

import dev.openfeature.api.types.ImmutableMetadata as ApiImmutableMetadata;
import java.util.Map;

/**
 * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.ImmutableMetadata;
 * ImmutableMetadata metadata = ImmutableMetadata.builder()
 *     .addString("key", "value")
 *     .build();
 *
 * // After
 * import dev.openfeature.api.types.ImmutableMetadata;
 * ImmutableMetadata metadata = ImmutableMetadata.builder()
 *     .string("key", "value")
 *     .build();
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@SuppressWarnings("deprecation")
public final class ImmutableMetadata {

    private final ApiImmutableMetadata delegate;

    private ImmutableMetadata(ApiImmutableMetadata delegate) {
        this.delegate = delegate;
    }

    // Delegate methods to new implementation
    public Boolean getBoolean(String key) {
        return delegate.getBoolean(key);
    }

    public String getString(String key) {
        return delegate.getString(key);
    }

    public Integer getInteger(String key) {
        return delegate.getInteger(key);
    }

    public Long getLong(String key) {
        return delegate.getLong(key);
    }

    public Float getFloat(String key) {
        return delegate.getFloat(key);
    }

    public Double getDouble(String key) {
        return delegate.getDouble(key);
    }

    public Map<String, Object> asMap() {
        return delegate.asMap();
    }

    /**
     * Convert to the new API implementation.
     * @return The underlying API implementation
     */
    public ApiImmutableMetadata toApiMetadata() {
        return delegate;
    }

    /**
     * Create from the new API implementation.
     * @param apiMetadata The new API metadata
     * @return The compatibility wrapper
     */
    public static ImmutableMetadata fromApiMetadata(ApiImmutableMetadata apiMetadata) {
        return new ImmutableMetadata(apiMetadata);
    }

    /**
     * Builder pattern for backward compatibility.
     * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata#builder()} directly.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static ImmutableMetadataBuilder builder() {
        return new ImmutableMetadataBuilder();
    }

    /**
     * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata.Builder} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final class ImmutableMetadataBuilder {
        private final ApiImmutableMetadata.Builder apiBuilder = ApiImmutableMetadata.builder();

        /**
         * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata.Builder#string(String, String)} instead.
         */
        @Deprecated(since = "2.0.0", forRemoval = true)
        public ImmutableMetadataBuilder addString(String key, String value) {
            apiBuilder.string(key, value);
            return this;
        }

        /**
         * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata.Builder#integer(String, Integer)} instead.
         */
        @Deprecated(since = "2.0.0", forRemoval = true)
        public ImmutableMetadataBuilder addInteger(String key, Integer value) {
            apiBuilder.integer(key, value);
            return this;
        }

        /**
         * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata.Builder#longValue(String, Long)} instead.
         */
        @Deprecated(since = "2.0.0", forRemoval = true)
        public ImmutableMetadataBuilder addLong(String key, Long value) {
            apiBuilder.longValue(key, value);
            return this;
        }

        /**
         * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata.Builder#floatValue(String, Float)} instead.
         */
        @Deprecated(since = "2.0.0", forRemoval = true)
        public ImmutableMetadataBuilder addFloat(String key, Float value) {
            apiBuilder.floatValue(key, value);
            return this;
        }

        /**
         * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata.Builder#doubleValue(String, Double)} instead.
         */
        @Deprecated(since = "2.0.0", forRemoval = true)
        public ImmutableMetadataBuilder addDouble(String key, Double value) {
            apiBuilder.doubleValue(key, value);
            return this;
        }

        /**
         * @deprecated Use {@link dev.openfeature.api.types.ImmutableMetadata.Builder#booleanValue(String, Boolean)} instead.
         */
        @Deprecated(since = "2.0.0", forRemoval = true)
        public ImmutableMetadataBuilder addBoolean(String key, Boolean value) {
            apiBuilder.booleanValue(key, value);
            return this;
        }

        public ImmutableMetadata build() {
            return new ImmutableMetadata(apiBuilder.build());
        }
    }
}