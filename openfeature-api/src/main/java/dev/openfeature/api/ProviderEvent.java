package dev.openfeature.api;

/**
 * Provider event types.
 */
public enum ProviderEvent {
    PROVIDER_READY,
    PROVIDER_CONFIGURATION_CHANGED,
    PROVIDER_ERROR,
    PROVIDER_STALE;
}
