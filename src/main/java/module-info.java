module dev.openfeature.sdk {
    requires static lombok;
    requires org.slf4j;
    requires com.github.spotbugs.annotations;

    exports dev.openfeature.sdk;
    exports dev.openfeature.sdk.exceptions;
    exports dev.openfeature.sdk.hooks.logging;
    exports dev.openfeature.sdk.providers.memory;
}
