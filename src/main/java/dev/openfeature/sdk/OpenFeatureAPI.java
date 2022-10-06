package dev.openfeature.sdk;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A global singleton which holds base configuration for the OpenFeature library.
 * Configuration here will be shared across all {@link Client}s.
 */
public class OpenFeatureAPI {
    private static OpenFeatureAPI api;
    @Getter
    @Setter
    private FeatureProvider provider;
    @Getter
    @Setter
    private EvaluationContext evaluationContext;
    @Getter
    private List<Hook> apiHooks;

    public OpenFeatureAPI() {
        this.apiHooks = new ArrayList<>();
    }

    /**
     * Provisions the {@link OpenFeatureAPI} singleton (if needed) and returns it.
     * @return The singleton instance.
     */
    public static OpenFeatureAPI getInstance() {
        synchronized (OpenFeatureAPI.class) {
            if (api == null) {
                api = new OpenFeatureAPI();
            }
        }
        return api;
    }

    public Metadata getProviderMetadata() {
        return provider.getMetadata();
    }

    public Client getClient() {
        return getClient(null, null);
    }

    public Client getClient(@Nullable String name) {
        return getClient(name, null);
    }

    public Client getClient(@Nullable String name, @Nullable String version) {
        return new OpenFeatureClient(this, name, version);
    }

    public void addHooks(Hook... hooks) {
        this.apiHooks.addAll(Arrays.asList(hooks));
    }

    public void clearHooks() {
        this.apiHooks.clear();
    }
}
