module dev.openfeature.api {
    requires org.slf4j;
    requires com.github.spotbugs.annotations;

    exports dev.openfeature.api;
    exports dev.openfeature.api.exceptions;
    exports dev.openfeature.api.internal.noop;
    exports dev.openfeature.api.tracking;
    exports dev.openfeature.api.evaluation;
    exports dev.openfeature.api.types;
    exports dev.openfeature.api.events;
    exports dev.openfeature.api.lifecycle;
    exports dev.openfeature.api.internal;

    uses dev.openfeature.api.OpenFeatureAPIProvider;
}
