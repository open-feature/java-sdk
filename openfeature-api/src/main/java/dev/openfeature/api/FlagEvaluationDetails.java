package dev.openfeature.api;

import java.util.Objects;

/**
 * Contains information about how the provider resolved a flag, including the
 * resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
public class FlagEvaluationDetails<T> implements BaseEvaluation<T> {

    private final String flagKey;
    private final T value;
    private final String variant;
    private final String reason;
    private final ErrorCode errorCode;
    private final String errorMessage;
    private final ImmutableMetadata flagMetadata;

    /**
     * Private constructor for builder pattern only.
     */
    private FlagEvaluationDetails() {
        this(null, null, null, null, null, null, null);
    }

    /**
     * Private constructor for immutable FlagEvaluationDetails.
     *
     * @param flagKey the flag key
     * @param value the resolved value
     * @param variant the variant identifier
     * @param reason the reason for the evaluation result
     * @param errorCode the error code if applicable
     * @param errorMessage the error message if applicable
     * @param flagMetadata metadata associated with the flag
     */
    private FlagEvaluationDetails(
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


    public T getValue() {
        return value;
    }


    public String getVariant() {
        return variant;
    }


    public String getReason() {
        return reason;
    }


    public ErrorCode getErrorCode() {
        return errorCode;
    }


    public String getErrorMessage() {
        return errorMessage;
    }


    public ImmutableMetadata getFlagMetadata() {
        return flagMetadata;
    }


    public static <T> Builder<T> builder() {
        return new Builder<>();
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
    public static class Builder<T> {
        private String flagKey;
        private T value;
        private String variant;
        private String reason;
        private ErrorCode errorCode;
        private String errorMessage;
        private ImmutableMetadata flagMetadata = ImmutableMetadata.builder().build();

        public Builder<T> flagKey(String flagKey) {
            this.flagKey = flagKey;
            return this;
        }

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

        public FlagEvaluationDetails<T> build() {
            return new FlagEvaluationDetails<>(flagKey, value, variant, reason, errorCode, errorMessage, flagMetadata);
        }
    }
}
