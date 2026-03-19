package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ImmutableMetadata;
import dev.openfeature.sdk.ProviderEvaluation;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ProviderEvaluation} subtype returned by multi-provider strategies that carries
 * per-provider error details.
 *
 * <p>This type can represent both successful and failed evaluations. When a strategy exhausts
 * all providers without a successful result, the per-provider errors describe why each provider
 * failed. Custom strategies may also use this type for successful results to surface information
 * about providers that were skipped or failed before the successful one.
 *
 * <p>Usage:
 * <pre>{@code
 * ProviderEvaluation<String> result = strategy.evaluate(...);
 * if (result instanceof MultiProviderEvaluation<String> multiResult) {
 *     for (ProviderError error : multiResult.getProviderErrors()) {
 *         log.warn("Provider {} failed: {} - {}",
 *             error.getProviderName(), error.getErrorCode(), error.getErrorMessage());
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the flag being evaluated
 */
public class MultiProviderEvaluation<T> extends ProviderEvaluation<T> {

    private final List<ProviderError> providerErrors;

    private MultiProviderEvaluation(
            T value,
            String variant,
            String reason,
            ErrorCode errorCode,
            String errorMessage,
            ImmutableMetadata flagMetadata,
            List<ProviderError> providerErrors) {
        super(value, variant, reason, errorCode, errorMessage, flagMetadata);
        this.providerErrors =
                providerErrors != null ? Collections.unmodifiableList(providerErrors) : Collections.emptyList();
    }

    /**
     * Returns the per-provider error details.
     *
     * <p>Each entry describes why a specific provider failed during multi-provider evaluation.
     *
     * @return an unmodifiable list of per-provider errors, never {@code null}
     */
    public List<ProviderError> getProviderErrors() {
        return providerErrors;
    }

    /**
     * Create a new builder for {@link MultiProviderEvaluation}.
     *
     * @param <T> the flag value type
     * @return a new builder
     */
    public static <T> Builder<T> multiProviderBuilder() {
        return new Builder<>();
    }

    /**
     * Builder for {@link MultiProviderEvaluation}.
     *
     * @param <T> the flag value type
     */
    public static class Builder<T> {
        private T value;
        private String variant;
        private String reason;
        private ErrorCode errorCode;
        private String errorMessage;
        private ImmutableMetadata flagMetadata;
        private List<ProviderError> providerErrors;

        public Builder<T> value(T value) {
            this.value = value;
            return this;
        }

        public Builder<T> variant(String variant) {
            this.variant = variant;
            return this;
        }

        public Builder<T> reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder<T> errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder<T> flagMetadata(ImmutableMetadata flagMetadata) {
            this.flagMetadata = flagMetadata;
            return this;
        }

        public Builder<T> providerErrors(List<ProviderError> providerErrors) {
            this.providerErrors = providerErrors;
            return this;
        }

        public MultiProviderEvaluation<T> build() {
            return new MultiProviderEvaluation<>(
                    value, variant, reason, errorCode, errorMessage, flagMetadata, providerErrors);
        }
    }
}
