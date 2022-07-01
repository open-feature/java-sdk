package dev.openfeature.javasdk;

public interface EventHook {
    default void onEvent(String event) {}
}
