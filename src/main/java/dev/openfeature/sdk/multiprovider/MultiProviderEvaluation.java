package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ProviderEvaluation;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * A {@link ProviderEvaluation} subtype returned by multi-provider strategies that carries
 * per-provider error details.
 *
 * <p>This type can represent both successful and failed evaluations. When a strategy exhausts
 * all providers without a successful result, the per-provider errors describe why each provider
 * failed. Custom strategies may also use this type for successful results to surface information
 * about providers that were skipped or failed before the successful one.
 *
 * <p>Usage:
 * <pre>{@code
 * ProviderEvaluation<String> result = strategy.evaluate(...);
 * if (result instanceof MultiProviderEvaluation<String> multiResult) {
 *     for (ProviderError error : multiResult.getProviderErrors()) {
 *         log.warn("Provider {} failed: {} - {}",
 *             error.getProviderName(), error.getErrorCode(), error.getErrorMessage());
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the flag being evaluated
 */
@Getter
@SuperBuilder
public class MultiProviderEvaluation<T> extends ProviderEvaluation<T> {

    /**
     * Per-provider error details.
     *
     * <p>Each entry describes why a specific provider failed during multi-provider evaluation.
     * Defaults to an empty list when not set.
     */
    @Builder.Default
    private List<ProviderError> providerErrors = Collections.emptyList();
}
