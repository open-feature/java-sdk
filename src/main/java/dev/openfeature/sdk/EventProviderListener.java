package dev.openfeature.sdk;

@FunctionalInterface
interface EventProviderListener {
    void onEmit(ProviderEvent event, ProviderEventDetails details);
}
