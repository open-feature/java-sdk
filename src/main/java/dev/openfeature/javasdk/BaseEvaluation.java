package dev.openfeature.javasdk;

/**
 * This is a common interface between the evaluation results that providers return and what is given to the end users.
 * @param <T> The type of flag being evaluated.
 */
public interface BaseEvaluation<T> {
    /**
     * Returns the resolved value of the evaluation.
     * @return {T} the resolve value
     */
    T getValue();

    /**
     * Returns an identifier for this value, if applicable.
     * @return {String} value identifier
     */
    String getVariant();

    /**
     * Describes how we came to the value that we're returning.
     * @return {Reason}
     */
    String getReason();

    /**
     * The error code, if applicable. Should only be set when the Reason is ERROR.
     * @return {ErrorCode}
     */
    ErrorCode getErrorCode();

    /**
     * The error message (usually from exception.getMessage()), if applicable.
     * Should only be set when the Reason is ERROR.
     * @return {String}
     */
    String getErrorMessage();
}
