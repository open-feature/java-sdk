package dev.openfeature.sdk;

/**
 * Provides access to the future provider for the domain of the client.
 */
public interface ProviderAccessor {
    FeatureProvider getProvider();
    ProviderState getProviderState();
}
