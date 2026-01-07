package dev.openfeature.sdk.multiprovider;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import java.util.Map;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * First Successful Strategy.
 *
 * <p>Similar to “First Match”, except that errors from evaluated providers do not halt execution.
 * Instead, it returns the first successful result from a provider. If no provider successfully
 * responds, it returns a {@code GENERAL} error result.
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
        for (FeatureProvider provider : providers.values()) {
            try {
                ProviderEvaluation<T> res = providerFunction.apply(provider);
                if (res.getErrorCode() == null) {
                    // First successful result (no error code)
                    return res;
                }
            } catch (Exception ignored) {
                // swallow and continue; errors from individual providers
                // are not fatal for this strategy
            }
        }

        return ProviderEvaluation.<T>builder()
                .errorMessage("No provider successfully responded")
                .errorCode(ErrorCode.GENERAL)
                .build();
    }
}
