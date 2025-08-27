module dev.openfeature.sdk {
    requires org.slf4j;
    requires com.github.spotbugs.annotations;
    requires dev.openfeature.api;

    exports dev.openfeature.sdk;
    exports dev.openfeature.sdk.providers.memory;
    exports dev.openfeature.sdk.hooks.logging;

    provides dev.openfeature.api.OpenFeatureAPIProvider with
            dev.openfeature.sdk.DefaultOpenFeatureAPIProvider;
}
