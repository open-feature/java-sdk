package dev.openfeature.javasdk;

/**
 * We differ between the evaluation results that providers return and what is given to the end users. This is a common interface between them.
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
    Reason getReason();

    /**
     * The error code, if applicable. Should only be set when the Reason is ERROR.
     * @return {ErrorCode}
     */
    String getErrorCode();
}
