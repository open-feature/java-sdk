package dev.openfeature.sdk.isolated;

import dev.openfeature.sdk.OpenFeatureAPI;

/**
 * Factory for creating isolated OpenFeature API instances.
 *
 * <p>Each instance returned by {@link #createAPI()} maintains its own state,
 * including providers, evaluation context, hooks, event handlers, and
 * transaction context propagators. Instances do not share state with the
 * global singleton ({@link OpenFeatureAPI#getInstance()}) or with each other.
 *
 * <p>This is useful for dependency injection frameworks, testing scenarios,
 * and applications composed of multiple submodules requiring distinct providers.
 *
 * <p><strong>Spec references:</strong>
 * <ul>
 *   <li>Requirement 1.8.1 &mdash; factory function for isolated instances</li>
 *   <li>Requirement 1.8.3 &mdash; distinct package for discoverability</li>
 * </ul>
 *
 * @see <a href="https://openfeature.dev/specification/sections/flag-evaluation#18-isolated-api-instances">
 *     Spec &sect;1.8 &mdash; Isolated API Instances</a>
 */
public final class OpenFeatureAPIFactory {

    private OpenFeatureAPIFactory() {
        // utility class
    }

    /**
     * Creates a new, independent {@link OpenFeatureAPI} instance with fully
     * isolated state.
     *
     * <p>Usage:
     * <pre>{@code
     * OpenFeatureAPI api = OpenFeatureAPIFactory.createAPI();
     * api.setProvider(new MyProvider());
     * Client client = api.getClient();
     * }</pre>
     *
     * @return a new API instance
     * @see OpenFeatureAPI#createIsolated()
     */
    public static OpenFeatureAPI createAPI() {
        return OpenFeatureAPI.createIsolated();
    }
}
