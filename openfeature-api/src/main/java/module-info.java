module dev.openfeature.api {
    requires org.slf4j;
    requires com.github.spotbugs.annotations;

    exports dev.openfeature.api;
    exports dev.openfeature.api.exceptions;
    exports dev.openfeature.api.internal.noop;

    uses dev.openfeature.api.OpenFeatureAPIProvider;
}
