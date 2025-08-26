package dev.openfeature.api;

/**
 * Core interface for basic OpenFeature operations.
 * Provides client management and provider configuration.
 */
public interface OpenFeatureCore {
    /**
     * A factory function for creating new, OpenFeature client.
     * Clients can contain their own state (e.g. logger, hook, context).
     * Multiple clients can be used to segment feature flag configuration.
     * All un-named or unbound clients use the default provider.
     *
     * @return a new client instance
     */
    Client getClient();

    /**
     * A factory function for creating new domainless OpenFeature client.
     * Clients can contain their own state (e.g. logger, hook, context).
     * Multiple clients can be used to segment feature flag configuration.
     * If there is already a provider bound to this domain, this provider will be used.
     * Otherwise, the default provider is used until a provider is assigned to that domain.
     *
     * @param domain an identifier which logically binds clients with providers
     * @return a new client instance
     */
    Client getClient(String domain);

    /**
     * A factory function for creating new domainless OpenFeature client.
     * Clients can contain their own state (e.g. logger, hook, context).
     * Multiple clients can be used to segment feature flag configuration.
     * If there is already a provider bound to this domain, this provider will be used.
     * Otherwise, the default provider is used until a provider is assigned to that domain.
     *
     * @param domain  a identifier which logically binds clients with providers
     * @param version a version identifier
     * @return a new client instance
     */
    Client getClient(String domain, String version);

    /**
     * Set the default provider.
     * 
     * @param provider the provider to set as default
     */
    void setProvider(FeatureProvider provider);

    /**
     * Add a provider for a domain.
     *
     * @param domain   The domain to bind the provider to.
     * @param provider The provider to set.
     */
    void setProvider(String domain, FeatureProvider provider);

    /**
     * Get metadata about the default provider.
     *
     * @return the provider metadata
     */
    Metadata getProviderMetadata();

    /**
     * Get metadata about a registered provider using the client name.
     * An unbound or empty client name will return metadata from the default provider.
     *
     * @param domain an identifier which logically binds clients with providers
     * @return the provider metadata
     */
    Metadata getProviderMetadata(String domain);
}