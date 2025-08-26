package dev.openfeature.api;

import java.util.function.Consumer;

/**
 * Advanced/SDK-specific interface for OpenFeature operations.
 * Provides lifecycle management and event handling capabilities.
 * Typically only implemented by full SDK implementations.
 */
public interface OpenFeatureAdvanced {
    /**
     * Shut down and reset the current status of OpenFeature API.
     * This call cleans up all active providers and attempts to shut down internal
     * event handling mechanisms.
     * Once shut down is complete, API is reset and ready to use again.
     */
    void shutdown();

    /**
     * Register an event handler for when a provider becomes ready.
     *
     * @param handler Consumer to handle the event
     * @return api instance for method chaining
     */
    OpenFeatureAPI onProviderReady(Consumer<EventDetails> handler);

    /**
     * Register an event handler for when a provider's configuration changes.
     *
     * @param handler Consumer to handle the event
     * @return api instance for method chaining
     */
    OpenFeatureAPI onProviderConfigurationChanged(Consumer<EventDetails> handler);

    /**
     * Register an event handler for when a provider becomes stale.
     *
     * @param handler Consumer to handle the event
     * @return api instance for method chaining
     */
    OpenFeatureAPI onProviderStale(Consumer<EventDetails> handler);

    /**
     * Register an event handler for when a provider encounters an error.
     *
     * @param handler Consumer to handle the event
     * @return api instance for method chaining
     */
    OpenFeatureAPI onProviderError(Consumer<EventDetails> handler);

    /**
     * Register an event handler for a specific provider event.
     *
     * @param event   the provider event to listen for
     * @param handler Consumer to handle the event
     * @return api instance for method chaining
     */
    OpenFeatureAPI on(ProviderEvent event, Consumer<EventDetails> handler);

    /**
     * Remove an event handler for a specific provider event.
     *
     * @param event   the provider event to stop listening for
     * @param handler the handler to remove
     * @return api instance for method chaining
     */
    OpenFeatureAPI removeHandler(ProviderEvent event, Consumer<EventDetails> handler);
}