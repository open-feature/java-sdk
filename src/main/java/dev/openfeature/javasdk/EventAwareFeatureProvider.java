package dev.openfeature.javasdk;

import java.util.function.Consumer;

public interface EventAwareFeatureProvider extends FeatureProvider {
    void setEventHookCallback(Consumer<String> eventHookConsumer);
}
