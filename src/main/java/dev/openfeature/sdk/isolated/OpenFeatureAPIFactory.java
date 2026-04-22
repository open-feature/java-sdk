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
 * <p>This class lives in a distinct package ({@code dev.openfeature.sdk.isolated})
 * to make isolated instances intentionally less discoverable than the global
 * singleton, reducing the chance of accidental use when the singleton would be
 * appropriate.
 *
 * <p>This is useful for dependency injection frameworks, testing scenarios,
 * and applications composed of multiple submodules requiring distinct providers.
 *
 * <p><strong>Spec references:</strong>
 * <ul>
 *   <li>Requirement 1.8.1 &mdash; factory function for isolated instances</li>
 *   <li>Requirement 1.8.3 &mdash; factory in a distinct package/module</li>
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
     */
    public static OpenFeatureAPI createAPI() {
        return new OpenFeatureAPI();
    }
}
