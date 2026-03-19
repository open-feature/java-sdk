package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * First Successful Strategy.
 *
 * <p>Similar to "First Match", except that errors from evaluated providers do not halt execution.
 * Instead, it returns the first successful result from a provider. If no provider successfully
 * responds, it returns a {@code GENERAL} error result that includes per-provider error details
 * describing why each provider failed.
 */
@Slf4j
@NoArgsConstructor
public class FirstSuccessfulStrategy implements Strategy {

    @Override
    public <T> ProviderEvaluation<T> evaluate(
            Map<String, FeatureProvider> providers,
            String key,
            T defaultValue,
            EvaluationContext ctx,
            Function<FeatureProvider, ProviderEvaluation<T>> providerFunction) {
        List<ProviderError> collectedErrors = new ArrayList<>();

        for (Map.Entry<String, FeatureProvider> entry : providers.entrySet()) {
            String providerName = entry.getKey();
            FeatureProvider provider = entry.getValue();
            try {
                ProviderEvaluation<T> res = providerFunction.apply(provider);
                if (res.getErrorCode() == null) {
                    // First successful result (no error code)
                    return res;
                }
                // Record error-coded result
                collectedErrors.add(ProviderError.fromResult(providerName, res.getErrorCode(), res.getErrorMessage()));
            } catch (Exception e) {
                // Record thrown exception
                collectedErrors.add(ProviderError.fromException(providerName, e));
            }
        }

        return MultiProviderEvaluation.<T>multiProviderBuilder()
                .errorMessage(
                        ProviderError.buildAggregateMessage("No provider successfully responded", collectedErrors))
                .errorCode(ErrorCode.GENERAL)
                .providerErrors(collectedErrors)
                .build();
    }
}
