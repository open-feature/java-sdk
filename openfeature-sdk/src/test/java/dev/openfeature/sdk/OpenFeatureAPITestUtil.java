package dev.openfeature.sdk;

import dev.openfeature.api.OpenFeatureAPI;

public class OpenFeatureAPITestUtil {

    private OpenFeatureAPITestUtil() {}

    public static OpenFeatureAPI createAPI() {
        return new DefaultOpenFeatureAPI();
    }
}
