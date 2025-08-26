package dev.openfeature.api;

import java.util.function.Consumer;

/**
 * Interface for provider event handling operations.
 * Provides event registration and management for provider state changes,
 * configuration updates, and other provider lifecycle events.
 */
public interface OpenFeatureEventHandling {
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
