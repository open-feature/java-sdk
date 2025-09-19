package dev.openfeature.api.events;

import dev.openfeature.api.ProviderEvent;
import java.util.function.Consumer;

/**
 * Interface for attaching event handlers.
 */
public interface EventBus<T> {

    /**
     * Add a handler for the {@link ProviderEvent#PROVIDER_READY} event.
     * Shorthand for {@link #on(ProviderEvent, Consumer)}
     *
     * @param handler behavior to add with this event
     * @return this
     */
    T onProviderReady(Consumer<EventDetails> handler);

    /**
     * Add a handler for the {@link ProviderEvent#PROVIDER_CONFIGURATION_CHANGED} event.
     * Shorthand for {@link #on(ProviderEvent, Consumer)}
     *
     * @param handler behavior to add with this event
     * @return this
     */
    T onProviderConfigurationChanged(Consumer<EventDetails> handler);

    /**
     * Add a handler for the {@link ProviderEvent#PROVIDER_STALE} event.
     * Shorthand for {@link #on(ProviderEvent, Consumer)}
     *
     * @param handler behavior to add with this event
     * @return this
     */
    T onProviderError(Consumer<EventDetails> handler);

    /**
     * Add a handler for the {@link ProviderEvent#PROVIDER_ERROR} event.
     * Shorthand for {@link #on(ProviderEvent, Consumer)}
     *
     * @param handler behavior to add with this event
     * @return this
     */
    T onProviderStale(Consumer<EventDetails> handler);

    /**
     * Add a handler for the specified {@link ProviderEvent}.
     *
     * @param event   event type
     * @param handler behavior to add with this event
     * @return this
     */
    T on(ProviderEvent event, Consumer<EventDetails> handler);

    /**
     * Remove the previously attached handler by reference.
     * If the handler doesn't exists, no-op.
     *
     * @param event   event type
     * @param handler to be removed
     * @return this
     */
    T removeHandler(ProviderEvent event, Consumer<EventDetails> handler);
}
