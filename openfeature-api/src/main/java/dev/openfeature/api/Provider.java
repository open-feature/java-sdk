package dev.openfeature.api;

import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.ProviderEvaluation;
import dev.openfeature.api.events.EventProvider;
import dev.openfeature.api.lifecycle.Hookable;
import dev.openfeature.api.lifecycle.Lifecycle;
import dev.openfeature.api.tracking.TrackingProvider;
import dev.openfeature.api.types.ProviderMetadata;
import dev.openfeature.api.types.Value;
import java.util.List;

/**
 * The interface implemented by upstream flag providers to resolve flags for
 * their service. If you want to support realtime events with your provider, you
 * should implement {@link EventProvider}
 */
public interface Provider extends Hookable<Provider>, Lifecycle, TrackingProvider {
    ProviderMetadata getMetadata();

    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx);

    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx);

    /**
     * This method is called before a provider is used to evaluate flags. Providers
     * can overwrite this method,
     * if they have special initialization needed prior being called for flag
     * evaluation.
     *
     * <p>
     * It is ok if the method is expensive as it is executed in the background. All
     * runtime exceptions will be
     * caught and logged.
     * </p>
     */
    default void initialize(EvaluationContext evaluationContext) throws Exception {
        // Intentionally left blank
    }

    /**
     * This method is called when a new provider is about to be used to evaluate
     * flags, or the SDK is shut down.
     * Providers can overwrite this method, if they have special shutdown actions
     * needed.
     *
     * <p>
     * It is ok if the method is expensive as it is executed in the background. All
     * runtime exceptions will be
     * caught and logged.
     * </p>
     */
    default void shutdown() {
        // Intentionally left blank
    }

    @Override
    default List<Hook<?>> getHooks() {
        return List.of();
    }

    @Override
    default Provider addHooks(Hook<?>... hooks) {
        return this;
    }
}
