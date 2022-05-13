package dev.openfeature.javasdk;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenFeatureAPI {
    @Getter @Setter private FeatureProvider provider;
    private static OpenFeatureAPI api;
    @Getter private List<Hook> apiHooks;

    public static OpenFeatureAPI getInstance() {
        synchronized (OpenFeatureAPI.class) {
            if (api == null) {
                api = new OpenFeatureAPI();
            }
        }
        return api;
    }

    public OpenFeatureAPI() {
        this.apiHooks = new ArrayList<>();
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

    public void registerHooks(Hook... hooks) {
        this.apiHooks.addAll(Arrays.asList(hooks));
    }

    public void clearHooks() {
        this.apiHooks.clear();
    }
}
