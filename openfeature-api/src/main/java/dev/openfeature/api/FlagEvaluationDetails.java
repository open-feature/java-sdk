package dev.openfeature.api;

import java.util.Objects;
import java.util.Optional;

/**
 * Contains information about how the provider resolved a flag, including the
 * resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
public class FlagEvaluationDetails<T> implements BaseEvaluation<T> {

    private String flagKey;
    private T value;
    private String variant;
    private String reason;
    private ErrorCode errorCode;
    private String errorMessage;
    private ImmutableMetadata flagMetadata;

    public FlagEvaluationDetails() {
        this.flagMetadata = ImmutableMetadata.builder().build();
    }

    /**
     * Constructs a FlagEvaluationDetails with the specified parameters.
     *
     * @param flagKey the flag key
     * @param value the resolved value
     * @param variant the variant identifier
     * @param reason the reason for the evaluation result
     * @param errorCode the error code if applicable
     * @param errorMessage the error message if applicable
     * @param flagMetadata metadata associated with the flag
     */
    public FlagEvaluationDetails(
            String flagKey,
            T value,
            String variant,
            String reason,
            ErrorCode errorCode,
            String errorMessage,
            ImmutableMetadata flagMetadata) {
        this.flagKey = flagKey;
        this.value = value;
        this.variant = variant;
        this.reason = reason;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.flagMetadata = flagMetadata != null
                ? flagMetadata
                : ImmutableMetadata.builder().build();
    }

    public String getFlagKey() {
        return flagKey;
    }

    public void setFlagKey(String flagKey) {
        this.flagKey = flagKey;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ImmutableMetadata getFlagMetadata() {
        return flagMetadata;
    }

    public void setFlagMetadata(ImmutableMetadata flagMetadata) {
        this.flagMetadata = flagMetadata;
    }

    public static <T> FlagEvaluationDetailsBuilder<T> builder() {
        return new FlagEvaluationDetailsBuilder<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FlagEvaluationDetails<?> that = (FlagEvaluationDetails<?>) obj;
        return Objects.equals(flagKey, that.flagKey)
                && Objects.equals(value, that.value)
                && Objects.equals(variant, that.variant)
                && Objects.equals(reason, that.reason)
                && errorCode == that.errorCode
                && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(flagMetadata, that.flagMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flagKey, value, variant, reason, errorCode, errorMessage, flagMetadata);
    }

    @Override
    public String toString() {
        return "FlagEvaluationDetails{" + "flagKey='"
                + flagKey + '\'' + ", value="
                + value + ", variant='"
                + variant + '\'' + ", reason='"
                + reason + '\'' + ", errorCode="
                + errorCode + ", errorMessage='"
                + errorMessage + '\'' + ", flagMetadata="
                + flagMetadata + '}';
    }

    /**
     * Builder class for creating instances of FlagEvaluationDetails.
     *
     * @param <T> the type of the flag value
     */
    public static class FlagEvaluationDetailsBuilder<T> {
        private String flagKey;
        private T value;
        private String variant;
        private String reason;
        private ErrorCode errorCode;
        private String errorMessage;
        private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();

        public FlagEvaluationDetailsBuilder<T> flagKey(String flagKey) {
            this.flagKey = flagKey;
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> value(T value) {
            this.value = value;
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> variant(String variant) {
            this.variant = variant;
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> reason(String reason) {
            this.reason = reason;
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public FlagEvaluationDetailsBuilder<T> flagMetadata(ImmutableMetadata flagMetadata) {
            this.flagMetadata = flagMetadata;
            return this;
        }

        public FlagEvaluationDetails<T> build() {
            return new FlagEvaluationDetails<>(flagKey, value, variant, reason, errorCode, errorMessage, flagMetadata);
        }
    }

    /**
     * Generate detail payload from the provider response.
     *
     * @param providerEval provider response
     * @param flagKey      key for the flag being evaluated
     * @param <T>          type of flag being returned
     * @return detail payload
     */
    public static <T> FlagEvaluationDetails<T> from(ProviderEvaluation<T> providerEval, String flagKey) {
        return FlagEvaluationDetails.<T>builder()
                .flagKey(flagKey)
                .value(providerEval.getValue())
                .variant(providerEval.getVariant())
                .reason(providerEval.getReason())
                .errorMessage(providerEval.getErrorMessage())
                .errorCode(providerEval.getErrorCode())
                .flagMetadata(Optional.ofNullable(providerEval.getFlagMetadata())
                        .orElse(ImmutableMetadata.builder().build()))
                .build();
    }
}
