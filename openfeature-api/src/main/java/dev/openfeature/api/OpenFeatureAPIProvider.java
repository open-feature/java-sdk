package dev.openfeature.api;

/**
 * ServiceLoader interface for OpenFeature API implementations.
 * Implementations of this interface can provide OpenFeature API instances
 * with different capabilities and priorities.
 */
public interface OpenFeatureAPIProvider {
    /**
     * Create an OpenFeature API implementation.
     *
     * @return the API implementation
     */
    OpenFeatureAPI createAPI();

    /**
     * Priority for this provider. Higher values take precedence.
     * This allows multiple implementations to coexist with clear precedence rules.
     *
     * @return priority value (default: 0)
     */
    default int getPriority() {
        return 0;
    }
}
