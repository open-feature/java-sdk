package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.OpenFeatureError;
import lombok.Builder;
import lombok.Value;

/**
 * Represents details about an error that occurred during a flag evaluation or other operations.
 * This class captures the exception, evaluation details, error code, and an error message.
 *
 * @param <T> The type of the value being evaluated in the {@link FlagEvaluationDetails}.
 */
@Value
@Builder
public class ErrorDetails<T> {
    Exception error;
    FlagEvaluationDetails<T> details;
    ErrorCode errorCode;
    String errorMessage;

    /**
     * Creates an {@code ErrorDetails} instance from the given {@link FlagEvaluationDetails}.
     * This method extracts the error message and error code from the provided details.
     *
     * @param details The {@link FlagEvaluationDetails} object containing flag evaluation information.
     * @param <T>     The type of the value being evaluated in the {@link FlagEvaluationDetails}.
     * @return An {@code ErrorDetails} object populated with the provided evaluation details.
     */
    public static <T> ErrorDetails<T> from(FlagEvaluationDetails<T> details) {
        return ErrorDetails.<T>builder()
                .details(details)
                .errorMessage(details.getErrorMessage())
                .errorCode(details.getErrorCode())
                .build();
    }

    /**
     * Creates an {@code ErrorDetails} instance from the given exception and {@link FlagEvaluationDetails}.
     * If the exception is an instance of {@link OpenFeatureError}, its error code is extracted
     * and set in the {@link FlagEvaluationDetails}. Otherwise, a general error code is used.
     * The exception's message is also set as the error message.
     *
     * @param exception The exception that occurred during the operation.
     * @param details   The {@link FlagEvaluationDetails} object containing flag evaluation information.
     * @param <T>       The type of the value being evaluated in the {@link FlagEvaluationDetails}.
     * @return An {@code ErrorDetails} object populated with the exception and evaluation details.
     */
    public static <T> ErrorDetails<T> from(Exception exception, FlagEvaluationDetails<T> details) {
        if (exception instanceof OpenFeatureError) {
            details.setErrorCode(((OpenFeatureError) exception).getErrorCode());
        } else {
            details.setErrorCode(ErrorCode.GENERAL);
        }
        details.setErrorMessage(exception.getMessage());
        return ErrorDetails.<T>builder()
                .error(exception)
                .errorMessage(exception.getMessage())
                .errorCode(details.getErrorCode())
                .build();
    }
}
