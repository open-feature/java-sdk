package dev.openfeature.sdk;

import dev.openfeature.api.Provider;

/**
 * @deprecated Use {@link dev.openfeature.api.Provider} instead.
 * This interface will be removed in v2.1.0.
 *
 * <p>Migration guide:
 * <pre>{@code
 * // Before
 * import dev.openfeature.sdk.FeatureProvider;
 * public class MyProvider implements FeatureProvider { }
 *
 * // After
 * import dev.openfeature.api.Provider;
 * public class MyProvider implements Provider { }
 * }</pre>
 *
 * @since 2.0.0
 */
@Deprecated(since = "2.0.0", forRemoval = true)
@SuppressWarnings("deprecation")
public interface FeatureProvider extends Provider {
    // This interface now extends the new Provider interface
    // All existing implementations will continue to work
    // but should migrate to dev.openfeature.api.Provider
}