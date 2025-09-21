package dev.openfeature.sdk;

import dev.openfeature.api.evaluation.EvaluationClient;

/**
 * @deprecated Use {@link dev.openfeature.api.evaluation.EvaluationClient} instead.
 * This interface will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.Features;
 * Features client = OpenFeature.getClient();
 *
 * // After
 * import dev.openfeature.api.evaluation.EvaluationClient;
 * EvaluationClient client = OpenFeature.getClient();
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@SuppressWarnings("deprecation")
public interface Features extends EvaluationClient {
    // This interface now extends the new EvaluationClient interface
    // All existing usage will continue to work
    // but should migrate to dev.openfeature.api.evaluation.EvaluationClient
}