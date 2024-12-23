package dev.openfeature.sdk;

import java.util.Map;
import java.util.Optional;

/**
 * An extension point which can run around flag resolution. They are intended to be used as a way to add custom logic
 * to the lifecycle of flag evaluation.
 *
 * @param <T> The type of the flag being evaluated.
 */
public interface Hook<T> {
    /**
     * Runs before flag is resolved.
     *
     * @param ctx   Information about the particular flag evaluation
     * @param hints An immutable mapping of data for users to communicate to the hooks.
     * @return An optional {@link EvaluationContext}. If returned, it will be merged with the EvaluationContext
     *     instances from other hooks, the client and API.
     */
    default Optional<EvaluationContext> before(HookContext<T> ctx, Map<String, Object> hints) {
        return Optional.empty();
    }

    /**
     * Runs after a flag is resolved.
     *
     * @param ctx     Information about the particular flag evaluation
     * @param details Information about how the flag was resolved, including any resolved values.
     * @param hints   An immutable mapping of data for users to communicate to the hooks.
     */
    default void after(HookContext<T> ctx, FlagEvaluationDetails<T> details, Map<String, Object> hints) {
    }

    /**
     * Run when evaluation encounters an error. This will always run. Errors thrown will be swallowed.
     *
     * @param ctx   Information about the particular flag evaluation
     * @param error The exception that was thrown.
     * @param hints An immutable mapping of data for users to communicate to the hooks.
     */
    default void error(HookContext<T> ctx, Exception error, Map<String, Object> hints) {
    }

    /**
     * Run after flag evaluation, including any error processing. This will always run. Errors will be swallowed.
     *
     * @param ctx   Information about the particular flag evaluation
     * @param hints An immutable mapping of data for users to communicate to the hooks.
     */
    default void finallyAfter(HookContext<T> ctx, FlagEvaluationDetails<T> details, Map<String, Object> hints) {
    }

    default boolean supportsFlagValueType(FlagValueType flagValueType) {
        return true;
    }
}
