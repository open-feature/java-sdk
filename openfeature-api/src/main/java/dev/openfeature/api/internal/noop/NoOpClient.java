package dev.openfeature.api.internal.noop;

import dev.openfeature.api.Client;
import dev.openfeature.api.ClientMetadata;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.EventDetails;
import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.api.FlagEvaluationOptions;
import dev.openfeature.api.Hook;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.Reason;
import dev.openfeature.api.TrackingEventDetails;
import dev.openfeature.api.Value;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * No-operation implementation of Client that provides safe defaults.
 * All flag evaluations return default values and all operations are safe no-ops.
 *
 * <p><strong>This is an internal implementation class and should not be used directly by external users.</strong>
 */
public class NoOpClient implements Client {

    @Override
    public ClientMetadata getMetadata() {
        return () -> "no-op";
    }

    @Override
    public EvaluationContext getEvaluationContext() {
        return EvaluationContext.EMPTY;
    }

    @Override
    public Client setEvaluationContext(EvaluationContext ctx) {
        return this; // No-op - return self for chaining
    }

    @Override
    public Client addHooks(Hook... hooks) {
        return this; // No-op - return self for chaining
    }

    @Override
    public List<Hook> getHooks() {
        return Collections.emptyList();
    }

    @Override
    public ProviderState getProviderState() {
        return ProviderState.READY; // Always ready since it's a no-op
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue) {
        return FlagEvaluationDetails.<Boolean>builder()
                .flagKey(key)
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(String key, Boolean defaultValue, EvaluationContext ctx) {
        return getBooleanDetails(key, defaultValue);
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(
            String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getBooleanDetails(key, defaultValue);
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public Boolean getBooleanValue(String key, Boolean defaultValue, EvaluationContext ctx) {
        return defaultValue;
    }

    @Override
    public Boolean getBooleanValue(
            String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return defaultValue;
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue) {
        return FlagEvaluationDetails.<String>builder()
                .flagKey(key)
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(String key, String defaultValue, EvaluationContext ctx) {
        return getStringDetails(key, defaultValue);
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(
            String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getStringDetails(key, defaultValue);
    }

    @Override
    public String getStringValue(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public String getStringValue(String key, String defaultValue, EvaluationContext ctx) {
        return defaultValue;
    }

    @Override
    public String getStringValue(
            String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return defaultValue;
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue) {
        return FlagEvaluationDetails.<Integer>builder()
                .flagKey(key)
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(String key, Integer defaultValue, EvaluationContext ctx) {
        return getIntegerDetails(key, defaultValue);
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(
            String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getIntegerDetails(key, defaultValue);
    }

    @Override
    public Integer getIntegerValue(String key, Integer defaultValue) {
        return defaultValue;
    }

    @Override
    public Integer getIntegerValue(String key, Integer defaultValue, EvaluationContext ctx) {
        return defaultValue;
    }

    @Override
    public Integer getIntegerValue(
            String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return defaultValue;
    }

    @Override
    public FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue) {
        return FlagEvaluationDetails.<Double>builder()
                .flagKey(key)
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public FlagEvaluationDetails<Double> getDoubleDetails(String key, Double defaultValue, EvaluationContext ctx) {
        return getDoubleDetails(key, defaultValue);
    }

    @Override
    public FlagEvaluationDetails<Double> getDoubleDetails(
            String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getDoubleDetails(key, defaultValue);
    }

    @Override
    public Double getDoubleValue(String key, Double defaultValue) {
        return defaultValue;
    }

    @Override
    public Double getDoubleValue(String key, Double defaultValue, EvaluationContext ctx) {
        return defaultValue;
    }

    @Override
    public Double getDoubleValue(
            String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return defaultValue;
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue) {
        return FlagEvaluationDetails.<Value>builder()
                .flagKey(key)
                .value(defaultValue)
                .reason(Reason.DEFAULT.toString())
                .build();
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(String key, Value defaultValue, EvaluationContext ctx) {
        return getObjectDetails(key, defaultValue);
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(
            String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return getObjectDetails(key, defaultValue);
    }

    @Override
    public Value getObjectValue(String key, Value defaultValue) {
        return defaultValue;
    }

    @Override
    public Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx) {
        return defaultValue;
    }

    @Override
    public Value getObjectValue(String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return defaultValue;
    }

    @Override
    public void track(String eventName) {
        // No-op - silently ignore
    }

    @Override
    public void track(String eventName, EvaluationContext context) {
        // No-op - silently ignore
    }

    @Override
    public void track(String eventName, TrackingEventDetails details) {
        // No-op - silently ignore
    }

    @Override
    public void track(String eventName, EvaluationContext context, TrackingEventDetails details) {
        // No-op - silently ignore
    }

    @Override
    public Client onProviderReady(Consumer<EventDetails> handler) {
        return this; // No-op - return self for chaining
    }

    @Override
    public Client onProviderConfigurationChanged(Consumer<EventDetails> handler) {
        return this; // No-op - return self for chaining
    }

    @Override
    public Client onProviderStale(Consumer<EventDetails> handler) {
        return this; // No-op - return self for chaining
    }

    @Override
    public Client onProviderError(Consumer<EventDetails> handler) {
        return this; // No-op - return self for chaining
    }

    @Override
    public Client on(ProviderEvent event, Consumer<EventDetails> handler) {
        return this; // No-op - return self for chaining
    }

    @Override
    public Client removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        return this; // No-op - return self for chaining
    }
}
