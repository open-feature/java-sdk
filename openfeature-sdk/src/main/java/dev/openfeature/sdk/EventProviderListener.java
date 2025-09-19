package dev.openfeature.sdk;

import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.events.ProviderEventDetails;

/**
 * TBD.
 */
@FunctionalInterface
public interface EventProviderListener {
    void onEmit(ProviderEvent event, ProviderEventDetails details);
}
