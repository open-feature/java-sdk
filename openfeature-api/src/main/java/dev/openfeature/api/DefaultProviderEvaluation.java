package dev.openfeature.api;

import java.util.Objects;

/**
 * Contains information about how the a flag was evaluated, including the resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
class DefaultProviderEvaluation<T> implements ProviderEvaluation<T> {
    private final T value;
    private final String variant;
    private final String reason;
    private final ErrorCode errorCode;
    private final String errorMessage;
    private final Metadata flagMetadata;

    /**
     * Private constructor for builder pattern only.
     */
    DefaultProviderEvaluation() {
        this(null, null, null, null, null, null);
    }

    /**
     * Private constructor for immutable ProviderEvaluation.
     *
     * @param value the resolved value
     * @param variant the variant identifier
     * @param reason the reason for the evaluation result
     * @param errorCode the error code if applicable
     * @param errorMessage the error message if applicable
     * @param flagMetadata metadata associated with the flag
     */
    DefaultProviderEvaluation(
            T value, String variant, String reason, ErrorCode errorCode, String errorMessage, Metadata flagMetadata) {
        this.value = value;
        this.variant = variant;
        this.reason = reason;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.flagMetadata = flagMetadata != null ? flagMetadata : Metadata.EMPTY;
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

    public Metadata getFlagMetadata() {
        return flagMetadata;
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
        return Objects.equals(value, that.getValue())
                && Objects.equals(variant, that.getVariant())
                && Objects.equals(reason, that.getReason())
                && errorCode == that.getErrorCode()
                && Objects.equals(errorMessage, that.getErrorMessage())
                && Objects.equals(flagMetadata, that.getFlagMetadata());
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
}
