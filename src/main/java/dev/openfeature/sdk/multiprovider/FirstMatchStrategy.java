package dev.openfeature.sdk.multiprovider;

import static dev.openfeature.sdk.ErrorCode.FLAG_NOT_FOUND;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * First match strategy.
 *
 * <p>Return the first result returned by a provider.
 * <ul>
 *   <li>Skip providers that indicate they had no value due to {@code FLAG_NOT_FOUND}.</li>
 *   <li>On any other error code, return that error result.</li>
 *   <li>If a provider throws {@link FlagNotFoundError}, it is treated like {@code FLAG_NOT_FOUND}.</li>
 *   <li>If all providers report {@code FLAG_NOT_FOUND}, return a {@code FLAG_NOT_FOUND} error
 *       with per-provider error details.</li>
 * </ul>
 * As soon as a non-{@code FLAG_NOT_FOUND} result is returned by a provider (success or other error),
 * the rest of the operation short-circuits and does not call the remaining providers.
 */
@Slf4j
@NoArgsConstructor
public class FirstMatchStrategy implements Strategy {

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
                ErrorCode errorCode = res.getErrorCode();
                if (errorCode == null) {
                    // Successful evaluation
                    return res;
                }
                if (!FLAG_NOT_FOUND.equals(errorCode)) {
                    // Any non-FLAG_NOT_FOUND error bubbles up immediately
                    return res;
                }
                // FLAG_NOT_FOUND: record and skip to next provider
                collectedErrors.add(ProviderError.fromResult(providerName, FLAG_NOT_FOUND, res.getErrorMessage()));
            } catch (FlagNotFoundError e) {
                // Treat thrown FlagNotFoundError like a FLAG_NOT_FOUND result
                collectedErrors.add(ProviderError.fromException(providerName, e));
            }
        }

        // All providers either threw or returned FLAG_NOT_FOUND
        return MultiProviderEvaluation.<T>multiProviderBuilder()
                .errorMessage(ProviderError.buildAggregateMessage("Flag not found in any provider", collectedErrors))
                .errorCode(FLAG_NOT_FOUND)
                .providerErrors(collectedErrors)
                .build();
    }
}
