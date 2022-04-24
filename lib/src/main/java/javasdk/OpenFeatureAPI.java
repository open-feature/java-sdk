package javasdk;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

public class OpenFeatureAPI {
    @Getter @Setter private FeatureProvider provider;
    private static OpenFeatureAPI api;

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

    public Client getClient(@Nullable String name) {
        return getClient(name, null);
    }

    public Client getClient(@Nullable String name, @Nullable String version) {
        return new OpenFeatureClient(this, name, version);
    }

}
