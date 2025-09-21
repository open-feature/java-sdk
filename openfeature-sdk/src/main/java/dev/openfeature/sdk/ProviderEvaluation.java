package dev.openfeature.sdk;

import dev.openfeature.api.evaluation.ProviderEvaluation as ApiProviderEvaluation;
import dev.openfeature.api.types.ImmutableMetadata;
import dev.openfeature.api.ErrorCode as ApiErrorCode;

/**
 * @deprecated Use {@link dev.openfeature.api.evaluation.ProviderEvaluation} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before (mutable with Lombok)
 * ProviderEvaluation<String> eval = new ProviderEvaluation<>();
 * eval.setValue("test");
 * eval.setVariant("variant1");
 *
 * // After (immutable with builder)
 * ProviderEvaluation<String> eval = ProviderEvaluation.<String>builder()
 *     .value("test")
 *     .variant("variant1")
 *     .build();
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@SuppressWarnings("deprecation")
public final class ProviderEvaluation<T> {

    private final ApiProviderEvaluation<T> delegate;

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public ProviderEvaluation() {
        this.delegate = ApiProviderEvaluation.<T>builder().build();
    }

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public ProviderEvaluation(T value, String variant, String reason,
                             ApiErrorCode errorCode, String errorMessage,
                             dev.openfeature.sdk.ImmutableMetadata flagMetadata) {
        ImmutableMetadata apiMetadata = flagMetadata != null ? flagMetadata.toApiMetadata() : null;

        this.delegate = ApiProviderEvaluation.<T>builder()
            .value(value)
            .variant(variant)
            .reason(reason)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .flagMetadata(apiMetadata)
            .build();
    }

    // Delegate getters to new implementation
    public T getValue() {
        return delegate.getValue();
    }

    public String getVariant() {
        return delegate.getVariant();
    }

    public String getReason() {
        return delegate.getReason();
    }

    public dev.openfeature.api.ErrorCode getErrorCode() {
        return delegate.getErrorCode();
    }

    public String getErrorMessage() {
        return delegate.getErrorMessage();
    }

    public dev.openfeature.sdk.ImmutableMetadata getFlagMetadata() {
        ImmutableMetadata apiMetadata = delegate.getFlagMetadata();
        return apiMetadata != null ? dev.openfeature.sdk.ImmutableMetadata.fromApiMetadata(apiMetadata) : null;
    }

    // Throw helpful exceptions for deprecated setters

    /**
     * @deprecated ProviderEvaluation is now immutable. Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setValue(T value) {
        throw new UnsupportedOperationException(
            "ProviderEvaluation is now immutable. Use ProviderEvaluation.<T>builder().value(value).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated ProviderEvaluation is now immutable. Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setVariant(String variant) {
        throw new UnsupportedOperationException(
            "ProviderEvaluation is now immutable. Use ProviderEvaluation.<T>builder().variant(variant).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated ProviderEvaluation is now immutable. Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setReason(String reason) {
        throw new UnsupportedOperationException(
            "ProviderEvaluation is now immutable. Use ProviderEvaluation.<T>builder().reason(reason).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated ProviderEvaluation is now immutable. Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setErrorCode(ApiErrorCode errorCode) {
        throw new UnsupportedOperationException(
            "ProviderEvaluation is now immutable. Use ProviderEvaluation.<T>builder().errorCode(errorCode).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated ProviderEvaluation is now immutable. Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setErrorMessage(String errorMessage) {
        throw new UnsupportedOperationException(
            "ProviderEvaluation is now immutable. Use ProviderEvaluation.<T>builder().errorMessage(errorMessage).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated ProviderEvaluation is now immutable. Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setFlagMetadata(dev.openfeature.sdk.ImmutableMetadata flagMetadata) {
        throw new UnsupportedOperationException(
            "ProviderEvaluation is now immutable. Use ProviderEvaluation.<T>builder().flagMetadata(flagMetadata).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * Provide access to the new API implementation for internal use.
     * @return The underlying API implementation
     */
    public ApiProviderEvaluation<T> toApiProviderEvaluation() {
        return delegate;
    }

    /**
     * Builder pattern for backward compatibility.
     * @deprecated Use {@link dev.openfeature.api.evaluation.ProviderEvaluation#builder()} directly.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static <T> ProviderEvaluationBuilder<T> builder() {
        return new ProviderEvaluationBuilder<>();
    }

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.ProviderEvaluation.Builder} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final class ProviderEvaluationBuilder<T> {
        private final ApiProviderEvaluation.Builder<T> apiBuilder = ApiProviderEvaluation.builder();

        public ProviderEvaluationBuilder<T> value(T value) {
            apiBuilder.value(value);
            return this;
        }

        public ProviderEvaluationBuilder<T> variant(String variant) {
            apiBuilder.variant(variant);
            return this;
        }

        public ProviderEvaluationBuilder<T> reason(String reason) {
            apiBuilder.reason(reason);
            return this;
        }

        public ProviderEvaluationBuilder<T> errorCode(ApiErrorCode errorCode) {
            apiBuilder.errorCode(errorCode);
            return this;
        }

        public ProviderEvaluationBuilder<T> errorMessage(String errorMessage) {
            apiBuilder.errorMessage(errorMessage);
            return this;
        }

        public ProviderEvaluationBuilder<T> flagMetadata(dev.openfeature.sdk.ImmutableMetadata flagMetadata) {
            ImmutableMetadata apiMetadata = flagMetadata != null ? flagMetadata.toApiMetadata() : null;
            apiBuilder.flagMetadata(apiMetadata);
            return this;
        }

        public ProviderEvaluation<T> build() {
            ApiProviderEvaluation<T> apiEval = apiBuilder.build();
            return fromApiProviderEvaluation(apiEval);
        }
    }

    /**
     * Create a deprecated ProviderEvaluation from the new API implementation.
     */
    private static <T> ProviderEvaluation<T> fromApiProviderEvaluation(ApiProviderEvaluation<T> apiEval) {
        ProviderEvaluation<T> result = new ProviderEvaluation<>();
        result.delegate = apiEval;
        return result;
    }
}