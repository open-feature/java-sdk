package javasdk;

import lombok.Setter;
import lombok.Getter;

import javax.annotation.Nullable;

public class OpenFeatureAPI {
    @Getter @Setter private FeatureProvider provider;

    public static OpenFeatureAPI getInstance() {
        return new OpenFeatureAPI();
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
