package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.List;

/**
 * The interface implemented by upstream flag providers to resolve flags for
 * their service. If you want to support realtime events with your provider, you
 * should extend {@link EventProvider}
 */
public interface FeatureProvider {

    /** Maximum integer losslessly representable as an IEEE-754 double: 2^53 - 1. */
    long MAX_SAFE_INTEGER = 9_007_199_254_740_991L;

    /**
     * Returns provider-identifying metadata (typically the provider name).
     *
     * @return provider metadata
     */
    Metadata getMetadata();

    /**
     * Returns provider-defined hooks that run alongside API/client/invocation hooks during
     * flag evaluation. Provider hooks are managed by the provider, not the application author.
     *
     * @return list of provider hooks; empty by default
     */
    default List<Hook> getProviderHooks() {
        return new ArrayList<>();
    }

    /**
     * Resolves a boolean flag value.
     *
     * @param key flag key
     * @param defaultValue value to return in the {@link ProviderEvaluation} if resolution fails
     * @param ctx merged evaluation context (may be empty, never {@code null})
     * @return provider evaluation containing the resolved value or an error
     */
    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx);

    /**
     * Resolves a string flag value.
     *
     * @param key flag key
     * @param defaultValue value to return in the {@link ProviderEvaluation} if resolution fails
     * @param ctx merged evaluation context (may be empty, never {@code null})
     * @return provider evaluation containing the resolved value or an error
     */
    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx);

    /**
     * Resolves a 32-bit integer flag value. For flags whose values may exceed
     * {@link Integer#MAX_VALUE}, use {@link #getLongEvaluation} instead.
     *
     * @param key flag key
     * @param defaultValue value to return in the {@link ProviderEvaluation} if resolution fails
     * @param ctx merged evaluation context (may be empty, never {@code null})
     * @return provider evaluation containing the resolved value or an error
     */
    ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx);

    /**
     * Resolves a double-precision floating-point flag value.
     *
     * @param key flag key
     * @param defaultValue value to return in the {@link ProviderEvaluation} if resolution fails
     * @param ctx merged evaluation context (may be empty, never {@code null})
     * @return provider evaluation containing the resolved value or an error
     */
    ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx);

    /**
     * Resolves a 64-bit integer (Long) flag value.
     *
     * <p>The default implementation delegates to {@link #getDoubleEvaluation} and returns a
     * {@link ProviderEvaluation} with {@link ErrorCode#TYPE_MISMATCH} for values outside the
     * safe-integer range ({@code [-(2^53 - 1), 2^53 - 1]}) or non-integral doubles (NaN,
     * +/-Infinity, fractional). Providers that natively support 64-bit integer flags should
     * override this method.
     *
     * @param key flag key
     * @param defaultValue value to return in the {@link ProviderEvaluation} if resolution fails
     * @param ctx merged evaluation context (may be empty, never {@code null})
     * @return provider evaluation containing the resolved value or an error
     */
    default ProviderEvaluation<Long> getLongEvaluation(String key, Long defaultValue, EvaluationContext ctx) {
        if (defaultValue != null && !isWithinSafeRange(defaultValue)) {
            return longError(
                    defaultValue,
                    "Default value " + defaultValue
                            + " exceeds safe integer range [-(2^53 - 1), 2^53 - 1] for double-backed long evaluation");
        }

        Double doubleDefault = defaultValue == null ? null : (double) defaultValue;
        ProviderEvaluation<Double> result = getDoubleEvaluation(key, doubleDefault, ctx);

        Double boxed = result.getValue();
        Long longValue;
        if (boxed == null) {
            longValue = defaultValue;
        } else {
            double value = boxed;
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return longError(defaultValue, "Cannot convert " + value + " to long", result);
            }
            if (value != Math.floor(value)) {
                return longError(defaultValue, "Cannot convert fractional value " + value + " to long", result);
            }
            if (Math.abs(value) > MAX_SAFE_INTEGER) {
                return longError(
                        defaultValue,
                        "Value " + value + " exceeds safe integer range [-(2^53 - 1), 2^53 - 1] for long",
                        result);
            }
            longValue = (long) value;
        }

        return ProviderEvaluation.<Long>builder()
                .value(longValue)
                .reason(result.getReason())
                .variant(result.getVariant())
                .errorCode(result.getErrorCode())
                .errorMessage(result.getErrorMessage())
                .flagMetadata(result.getFlagMetadata())
                .build();
    }

    // avoid Math.abs; Math.abs(Long.MIN_VALUE) == Long.MIN_VALUE (two's-complement overflow)
    private static boolean isWithinSafeRange(long value) {
        return value >= -MAX_SAFE_INTEGER && value <= MAX_SAFE_INTEGER;
    }

    private static ProviderEvaluation<Long> longError(Long defaultValue, String message) {
        return ProviderEvaluation.<Long>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .errorCode(ErrorCode.TYPE_MISMATCH)
                .errorMessage(message)
                .build();
    }

    // preserve upstream metadata/variant; override with type error
    private static ProviderEvaluation<Long> longError(
            Long defaultValue, String message, ProviderEvaluation<Double> upstream) {
        return ProviderEvaluation.<Long>builder()
                .value(defaultValue)
                .reason(Reason.ERROR.toString())
                .errorCode(ErrorCode.TYPE_MISMATCH)
                .errorMessage(message)
                .variant(upstream.getVariant())
                .flagMetadata(upstream.getFlagMetadata())
                .build();
    }

    /**
     * Resolves a structured (object) flag value. Values are wrapped in {@link Value} which can
     * carry booleans, strings, numbers, structures, and lists.
     *
     * @param key flag key
     * @param defaultValue value to return in the {@link ProviderEvaluation} if resolution fails
     * @param ctx merged evaluation context (may be empty, never {@code null})
     * @return provider evaluation containing the resolved value or an error
     */
    ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx);

    /**
     * Called once before a provider is used to evaluate flags. Providers can override this method
     * if they have special initialization needed prior to being called for flag evaluation.
     *
     * <p>It is ok if the method is expensive; it is executed in the background. All runtime
     * exceptions will be caught and logged.
     *
     * @param evaluationContext the API-level evaluation context at the time of initialization
     * @throws Exception any exception thrown here transitions the provider to
     *     {@link ProviderState#ERROR} (or {@link ProviderState#FATAL} for
     *     {@link dev.openfeature.sdk.exceptions.FatalError})
     */
    default void initialize(EvaluationContext evaluationContext) throws Exception {
        // Intentionally left blank
    }

    /**
     * Called when a provider is about to be replaced or the SDK is shutting down. Providers can
     * override this method if they have resources to release (background threads, connections,
     * caches, etc.).
     *
     * <p>It is ok if the method is expensive; it is executed in the background. All runtime
     * exceptions will be caught and logged.
     */
    default void shutdown() {
        // Intentionally left blank
    }

    /**
     * Returns a representation of the current readiness of the provider.
     * If the provider needs to be initialized, it should return {@link ProviderState#NOT_READY}.
     * If the provider is in an error state, it should return {@link ProviderState#ERROR}.
     * If the provider is functioning normally, it should return {@link ProviderState#READY}.
     *
     * <p><i>Providers which do not implement this method are assumed to be ready immediately.</i></p>
     *
     * @return ProviderState
     * @deprecated The state is handled by the SDK internally. Query the state from the {@link Client} instead.
     */
    @Deprecated
    default ProviderState getState() {
        return ProviderState.READY;
    }

    /**
     * Feature provider implementations can opt in for to support Tracking by implementing this method.
     *
     * @param eventName The name of the tracking event
     * @param context   Evaluation context used in flag evaluation (Optional)
     * @param details   Data pertinent to a particular tracking event (Optional)
     */
    default void track(String eventName, EvaluationContext context, TrackingEventDetails details) {}
}
