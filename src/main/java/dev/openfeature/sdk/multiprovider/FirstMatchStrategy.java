package dev.openfeature.sdk.multiprovider;

import static dev.openfeature.sdk.ErrorCode.FLAG_NOT_FOUND;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
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
 *   <li>If all providers report {@code FLAG_NOT_FOUND}, return a {@code FLAG_NOT_FOUND} error.</li>
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
        for (FeatureProvider provider : providers.values()) {
            try {
                ProviderEvaluation<T> res = providerFunction.apply(provider);
                ErrorCode errorCode = res.getErrorCode();
                if (errorCode == null) {
                    // Successful evaluation
                    return res;
                }
                if (!FLAG_NOT_FOUND.equals(errorCode)) {
                    // Any non-FLAG_NOT_FOUND error bubbles up
                    return res;
                }
                // else FLAG_NOT_FOUND: skip to next provider
            } catch (FlagNotFoundError ignored) {
                // do not log in hot path, just skip
            }
        }

        // All providers either threw or returned FLAG_NOT_FOUND
        return ProviderEvaluation.<T>builder()
                .errorMessage("Flag not found in any provider")
                .errorCode(FLAG_NOT_FOUND)
                .build();
    }
}
