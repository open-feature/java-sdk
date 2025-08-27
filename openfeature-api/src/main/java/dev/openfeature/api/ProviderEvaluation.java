package dev.openfeature.api;

import java.util.Objects;

/**
 * Contains information about how the a flag was evaluated, including the resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
public class ProviderEvaluation<T> implements BaseEvaluation<T> {
    private T value;
    private String variant;
    private String reason;
    private ErrorCode errorCode;
    private String errorMessage;
    private ImmutableMetadata flagMetadata;

    public ProviderEvaluation() {
        this.flagMetadata = ImmutableMetadata.builder().build();
    }

    /**
     * Constructs a ProviderEvaluation with the specified parameters.
     *
     * @param value the resolved value
     * @param variant the variant identifier
     * @param reason the reason for the evaluation result
     * @param errorCode the error code if applicable
     * @param errorMessage the error message if applicable
     * @param flagMetadata metadata associated with the flag
     */
    public ProviderEvaluation(
            T value,
            String variant,
            String reason,
            ErrorCode errorCode,
            String errorMessage,
            ImmutableMetadata flagMetadata) {
        this.value = value;
        this.variant = variant;
        this.reason = reason;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.flagMetadata = flagMetadata != null
                ? flagMetadata
                : ImmutableMetadata.builder().build();
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

    public static <T> ProviderEvaluationBuilder<T> builder() {
        return new ProviderEvaluationBuilder<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ProviderEvaluation<?> that = (ProviderEvaluation<?>) obj;
        return Objects.equals(value, that.value)
                && Objects.equals(variant, that.variant)
                && Objects.equals(reason, that.reason)
                && errorCode == that.errorCode
                && Objects.equals(errorMessage, that.errorMessage)
                && Objects.equals(flagMetadata, that.flagMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, variant, reason, errorCode, errorMessage, flagMetadata);
    }

    @Override
    public String toString() {
        return "ProviderEvaluation{" + "value="
                + value + ", variant='"
                + variant + '\'' + ", reason='"
                + reason + '\'' + ", errorCode="
                + errorCode + ", errorMessage='"
                + errorMessage + '\'' + ", flagMetadata="
                + flagMetadata + '}';
    }

    /**
     * Builder class for creating instances of ProviderEvaluation.
     *
     * @param <T> the type of the evaluation value
     */
    public static class ProviderEvaluationBuilder<T> {
        private T value;
        private String variant;
        private String reason;
        private ErrorCode errorCode;
        private String errorMessage;
        private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();

        public ProviderEvaluationBuilder<T> value(T value) {
            this.value = value;
            return this;
        }

        public ProviderEvaluationBuilder<T> variant(String variant) {
            this.variant = variant;
            return this;
        }

        public ProviderEvaluationBuilder<T> reason(String reason) {
            this.reason = reason;
            return this;
        }

        public ProviderEvaluationBuilder<T> errorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ProviderEvaluationBuilder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public ProviderEvaluationBuilder<T> flagMetadata(ImmutableMetadata flagMetadata) {
            this.flagMetadata = flagMetadata;
            return this;
        }

        public ProviderEvaluation<T> build() {
            return new ProviderEvaluation<>(value, variant, reason, errorCode, errorMessage, flagMetadata);
        }
    }
}
