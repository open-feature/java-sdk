package dev.openfeature.api.internal.noop;

import dev.openfeature.api.Client;
import dev.openfeature.api.Hook;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.Reason;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.FlagEvaluationDetails;
import dev.openfeature.api.evaluation.FlagEvaluationOptions;
import dev.openfeature.api.events.EventDetails;
import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;
import dev.openfeature.api.tracking.TrackingEventDetails;
import dev.openfeature.api.types.ClientMetadata;
import dev.openfeature.api.types.Value;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * No-operation implementation of Client that provides safe defaults.
 * All flag evaluations return default values and all operations are safe no-ops.
 *
 * <p><strong>This is an internal implementation class and should not be used directly by external users.</strong>
 */
@ExcludeFromGeneratedCoverageReport
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
    public Client addHooks(Hook<?>... hooks) {
        return this; // No-op - return self for chaining
    }

    @Override
    public List<Hook<?>> getHooks() {
        return Collections.emptyList();
    }

    @Override
    public ProviderState getProviderState() {
        return ProviderState.READY; // Always ready since it's a no-op
    }

    @Override
    public FlagEvaluationDetails<Boolean> getBooleanDetails(
            String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return FlagEvaluationDetails.of(key, defaultValue, Reason.DEFAULT);
    }

    @Override
    public FlagEvaluationDetails<String> getStringDetails(
            String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return FlagEvaluationDetails.of(key, defaultValue, Reason.DEFAULT);
    }

    @Override
    public FlagEvaluationDetails<Integer> getIntegerDetails(
            String key, Integer defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return FlagEvaluationDetails.of(key, defaultValue, Reason.DEFAULT);
    }

    @Override
    public FlagEvaluationDetails<Double> getDoubleDetails(
            String key, Double defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return FlagEvaluationDetails.of(key, defaultValue, Reason.DEFAULT);
    }

    @Override
    public FlagEvaluationDetails<Value> getObjectDetails(
            String key, Value defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
        return FlagEvaluationDetails.of(key, defaultValue, Reason.DEFAULT);
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
