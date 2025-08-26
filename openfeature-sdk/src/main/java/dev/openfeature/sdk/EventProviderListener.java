package dev.openfeature.sdk;

import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderEventDetails;

@FunctionalInterface
interface EventProviderListener {
    void onEmit(ProviderEvent event, ProviderEventDetails details);
}
