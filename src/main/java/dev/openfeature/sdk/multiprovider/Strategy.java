package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import java.util.List;
import java.util.function.Function;

/**
 * Strategy for determining how to evaluate a flag across multiple providers.
 *
 * <p>Implementations decide how to:
 * <ul>
 *   <li>Order or select providers</li>
 *   <li>Handle {@code FLAG_NOT_FOUND} results</li>
 *   <li>Handle errors and exceptions from providers</li>
 * </ul>
 */
public interface Strategy {

    /**
     * Evaluate a flag across multiple providers.
     *
     * @param providers      ordered map of provider name to provider
     * @param key            the flag key to evaluate
     * @param defaultValue   the default value to fall back to
     * @param ctx            the evaluation context (may be {@code null})
     * @param providerFunction function that executes the provider evaluation for the given key
     * @param <T>            the flag value type
     * @return the resolved {@link ProviderEvaluation}
     */
    <T> ProviderEvaluation<T> evaluate(
            List<FeatureProvider> providers,
            String key,
            T defaultValue,
            EvaluationContext ctx,
            Function<FeatureProvider, ProviderEvaluation<T>> providerFunction);
}
