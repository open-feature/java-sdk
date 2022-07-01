package dev.openfeature.javasdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;

/**
 * A global singleton which holds base configuration for the OpenFeature library.
 *
 * Configuration here will be shared across all {@link Client}s.
 */
public class OpenFeatureAPI {
    @Getter private FeatureProvider provider;

    public void setProvider(FeatureProvider provider) {
        this.provider = provider;
        // set the event hooks
        if (provider instanceof EventAwareFeatureProvider) {
            ((EventAwareFeatureProvider)provider).setEventHookCallback(event -> eventHooks.forEach(hook -> hook.onEvent(event)));
        }
    }
    private static OpenFeatureAPI api;
    @Getter private List<Hook> apiHooks = new ArrayList<>();

    @Getter private List<EventHook> eventHooks = new ArrayList<>();

    public static OpenFeatureAPI getInstance() {
        synchronized (OpenFeatureAPI.class) {
            if (api == null) {
                api = new OpenFeatureAPI();
            }
        }
        return api;
    }

    public Client getClient() {
        return getClient(null, null);
    }

    public Metadata getProviderMetadata() {
        return provider.getMetadata();
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

    public void addEventHooks(EventHook... hooks) {
        this.eventHooks.addAll(Arrays.asList(hooks));
    }

    public void clearHooks() {
        this.apiHooks.clear();
    }
}
