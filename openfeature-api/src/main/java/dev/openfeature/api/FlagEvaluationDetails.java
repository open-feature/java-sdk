package dev.openfeature.api;

/**
 * Contains information about how the provider resolved a flag, including the
 * resolved value.
 *
 * @param <T> the type of the flag being evaluated.
 */
public interface FlagEvaluationDetails<T> extends BaseEvaluation<T> {

    FlagEvaluationDetails<?> EMPTY = new DefaultFlagEvaluationDetails<>();

    String getFlagKey();

    static <T> FlagEvaluationDetails<T> of(String key, T value, Reason reason) {
        return of(key, value, null, reason);
    }

    static <T> FlagEvaluationDetails<T> of(String key, T value, String variant, Reason reason) {
        return of(key, value, variant, reason, null, null, null);
    }

    static <T> FlagEvaluationDetails<T> of(
            String key,
            T value,
            String variant,
            Reason reason,
            ErrorCode errorCode,
            String errorMessage,
            Metadata flagMetadata) {
        return of(key, value, variant, reason.toString(), errorCode, errorMessage, flagMetadata);
    }

    static <T> FlagEvaluationDetails<T> of(
            String key,
            T value,
            String variant,
            String reason,
            ErrorCode errorCode,
            String errorMessage,
            Metadata flagMetadata) {
        return new DefaultFlagEvaluationDetails<>(key, value, variant, reason, errorCode, errorMessage, flagMetadata);
    }
}
