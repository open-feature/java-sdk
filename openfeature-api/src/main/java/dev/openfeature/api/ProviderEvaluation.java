package dev.openfeature.api;

/**
 * Contains information about how the a flag was evaluated, including the resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
public interface ProviderEvaluation<T> extends BaseEvaluation<T> {

    static <T> ProviderEvaluation<T> of(T value, String variant, String reason, Metadata flagMetadata) {
        return of(value, variant, reason, null, null, flagMetadata);
    }

    static <T> ProviderEvaluation<T> of(
            T value, String variant, String reason, ErrorCode errorCode, String errorMessage, Metadata flagMetadata) {
        return new DefaultProviderEvaluation<T>(value, variant, reason, errorCode, errorMessage, flagMetadata);
    }

    static <T> ProviderEvaluation<T> of(ErrorCode errorCode, String errorMessage) {
        return of(null, null, Reason.ERROR.toString(), errorCode, errorMessage, null);
    }
}
