package dev.openfeature.sdk;

import dev.openfeature.api.evaluation.FlagEvaluationDetails as ApiFlagEvaluationDetails;
import dev.openfeature.api.evaluation.ProviderEvaluation as ApiProviderEvaluation;
import dev.openfeature.api.types.ImmutableMetadata;
import dev.openfeature.api.ErrorCode as ApiErrorCode;

/**
 * @deprecated Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails} instead.
 * This class will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before (mutable with Lombok)
 * FlagEvaluationDetails<String> details = new FlagEvaluationDetails<>();
 * details.setFlagKey("my-flag");
 * details.setValue("test");
 *
 * // After (immutable with builder)
 * FlagEvaluationDetails<String> details = FlagEvaluationDetails.<String>builder()
 *     .flagKey("my-flag")
 *     .value("test")
 *     .build();
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@SuppressWarnings("deprecation")
public final class FlagEvaluationDetails<T> {

    private final ApiFlagEvaluationDetails<T> delegate;

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public FlagEvaluationDetails() {
        this.delegate = ApiFlagEvaluationDetails.<T>builder().build();
    }

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public FlagEvaluationDetails(String flagKey, T value, String variant, String reason,
                                ApiErrorCode errorCode, String errorMessage,
                                dev.openfeature.sdk.ImmutableMetadata flagMetadata) {
        ImmutableMetadata apiMetadata = flagMetadata != null ? flagMetadata.toApiMetadata() : null;

        this.delegate = ApiFlagEvaluationDetails.<T>builder()
            .flagKey(flagKey)
            .value(value)
            .variant(variant)
            .reason(reason)
            .errorCode(errorCode)
            .errorMessage(errorMessage)
            .flagMetadata(apiMetadata)
            .build();
    }

    private FlagEvaluationDetails(ApiFlagEvaluationDetails<T> delegate) {
        this.delegate = delegate;
    }

    // Delegate getters to new implementation
    public String getFlagKey() {
        return delegate.getFlagKey();
    }

    public T getValue() {
        return delegate.getValue();
    }

    public String getVariant() {
        return delegate.getVariant();
    }

    public String getReason() {
        return delegate.getReason();
    }

    public ApiErrorCode getErrorCode() {
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
     * @deprecated FlagEvaluationDetails is now immutable. Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setFlagKey(String flagKey) {
        throw new UnsupportedOperationException(
            "FlagEvaluationDetails is now immutable. Use FlagEvaluationDetails.<T>builder().flagKey(flagKey).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated FlagEvaluationDetails is now immutable. Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setValue(T value) {
        throw new UnsupportedOperationException(
            "FlagEvaluationDetails is now immutable. Use FlagEvaluationDetails.<T>builder().value(value).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated FlagEvaluationDetails is now immutable. Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setVariant(String variant) {
        throw new UnsupportedOperationException(
            "FlagEvaluationDetails is now immutable. Use FlagEvaluationDetails.<T>builder().variant(variant).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated FlagEvaluationDetails is now immutable. Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setReason(String reason) {
        throw new UnsupportedOperationException(
            "FlagEvaluationDetails is now immutable. Use FlagEvaluationDetails.<T>builder().reason(reason).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated FlagEvaluationDetails is now immutable. Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setErrorCode(ApiErrorCode errorCode) {
        throw new UnsupportedOperationException(
            "FlagEvaluationDetails is now immutable. Use FlagEvaluationDetails.<T>builder().errorCode(errorCode).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated FlagEvaluationDetails is now immutable. Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setErrorMessage(String errorMessage) {
        throw new UnsupportedOperationException(
            "FlagEvaluationDetails is now immutable. Use FlagEvaluationDetails.<T>builder().errorMessage(errorMessage).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * @deprecated FlagEvaluationDetails is now immutable. Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} instead.
     * @throws UnsupportedOperationException always, with migration guidance
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setFlagMetadata(dev.openfeature.sdk.ImmutableMetadata flagMetadata) {
        throw new UnsupportedOperationException(
            "FlagEvaluationDetails is now immutable. Use FlagEvaluationDetails.<T>builder().flagMetadata(flagMetadata).build() instead. " +
            "See migration guide: https://docs.openfeature.dev/java-sdk/v2-migration"
        );
    }

    /**
     * Generate detail payload from the provider response.
     * @deprecated Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} directly instead.
     *
     * @param providerEval provider response
     * @param flagKey      key for the flag being evaluated
     * @param <T>          type of flag being returned
     * @return detail payload
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static <T> FlagEvaluationDetails<T> from(ProviderEvaluation<T> providerEval, String flagKey) {
        // Convert the compatibility ProviderEvaluation to API version if needed
        ApiProviderEvaluation<T> apiProviderEval = providerEval.toApiProviderEvaluation();

        ApiFlagEvaluationDetails<T> apiDetails = ApiFlagEvaluationDetails.<T>builder()
                .flagKey(flagKey)
                .value(apiProviderEval.getValue())
                .variant(apiProviderEval.getVariant())
                .reason(apiProviderEval.getReason())
                .errorMessage(apiProviderEval.getErrorMessage())
                .errorCode(apiProviderEval.getErrorCode())
                .flagMetadata(apiProviderEval.getFlagMetadata())
                .build();

        return new FlagEvaluationDetails<>(apiDetails);
    }

    /**
     * Provide access to the new API implementation for internal use.
     * @return The underlying API implementation
     */
    public ApiFlagEvaluationDetails<T> toApiFlagEvaluationDetails() {
        return delegate;
    }

    /**
     * Builder pattern for backward compatibility.
     * @deprecated Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails#builder()} directly.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static <T> FlagEvaluationDetailsBuilder<T> builder() {
        return new FlagEvaluationDetailsBuilder<>();
    }

    /**
     * @deprecated Use {@link dev.openfeature.api.evaluation.FlagEvaluationDetails.Builder} instead.
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public static final class FlagEvaluationDetailsBuilder<T> {
        private final ApiFlagEvaluationDetails.Builder<T> apiBuilder = ApiFlagEvaluationDetails.builder();

        public FlagEvaluationDetailsBuilder<T> flagKey(String flagKey) {
            apiBuilder.flagKey(flagKey);
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> value(T value) {
            apiBuilder.value(value);
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> variant(String variant) {
            apiBuilder.variant(variant);
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> reason(String reason) {
            apiBuilder.reason(reason);
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> errorCode(ApiErrorCode errorCode) {
            apiBuilder.errorCode(errorCode);
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> errorMessage(String errorMessage) {
            apiBuilder.errorMessage(errorMessage);
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> flagMetadata(dev.openfeature.sdk.ImmutableMetadata flagMetadata) {
            ImmutableMetadata apiMetadata = flagMetadata != null ? flagMetadata.toApiMetadata() : null;
            apiBuilder.flagMetadata(apiMetadata);
            return this;
        }

        public FlagEvaluationDetails<T> build() {
            ApiFlagEvaluationDetails<T> apiDetails = apiBuilder.build();
            return new FlagEvaluationDetails<>(apiDetails);
        }
    }
}