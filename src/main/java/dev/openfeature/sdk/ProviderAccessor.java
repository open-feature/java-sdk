package dev.openfeature.sdk;

@FunctionalInterface
public interface ProviderAccessor {
    FeatureProvider getProvider();
}
