package dev.openfeature.sdk;

/**
 * Provides access to the future provider for the domain of the client.
 */
@FunctionalInterface
interface ProviderAccessor {
    FeatureProviderStateManager getProviderStateManager();
}
