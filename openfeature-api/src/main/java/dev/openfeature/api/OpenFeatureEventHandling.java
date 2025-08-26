package dev.openfeature.api;

import java.util.function.Consumer;

/**
 * Interface for advanced event handling capabilities.
 * This interface provides domain-specific event handler management
 * which is typically used by SDK implementations but not required
 * for basic API usage.
 */
public interface OpenFeatureEventHandling {
    
    /**
     * Add event handlers for domain-specific provider events.
     * This method is used by SDK implementations to manage client-level event handlers.
     * 
     * @param domain the domain for which to add the handler
     * @param event the provider event to listen for
     * @param handler the event handler to add
     */
    void addHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler);
    
    /**
     * Remove event handlers for domain-specific provider events.
     * This method is used by SDK implementations to manage client-level event handlers.
     * 
     * @param domain the domain for which to remove the handler
     * @param event the provider event to stop listening for
     * @param handler the event handler to remove
     */
    void removeHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler);
}