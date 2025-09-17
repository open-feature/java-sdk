package dev.openfeature.api.internal.noop;

import dev.openfeature.api.Client;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.EventDetails;
import dev.openfeature.api.FeatureProvider;
import dev.openfeature.api.Hook;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderMetadata;
import dev.openfeature.api.TransactionContextPropagator;
import dev.openfeature.api.exceptions.OpenFeatureError;
import dev.openfeature.api.internal.ExcludeFromGeneratedCoverageReport;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * No-operation implementation of OpenFeatureAPI that provides safe defaults.
 * Used as a fallback when no actual implementation is available via ServiceLoader.
 * All operations are safe no-ops that won't affect application functionality.
 *
 * <p>Package-private to prevent direct instantiation by external users.
 */
@ExcludeFromGeneratedCoverageReport
public class NoOpOpenFeatureAPI extends OpenFeatureAPI {

    private static final NoOpClient NO_OP_CLIENT = new NoOpClient();
    private static final NoOpProvider NO_OP_PROVIDER = new NoOpProvider();
    private static final NoOpTransactionContextPropagator NO_OP_TRANSACTION_CONTEXT_PROPAGATOR =
            new NoOpTransactionContextPropagator();

    @Override
    public Client getClient() {
        return NO_OP_CLIENT;
    }

    @Override
    public Client getClient(String domain) {
        return NO_OP_CLIENT;
    }

    @Override
    public Client getClient(String domain, String version) {
        return NO_OP_CLIENT;
    }

    @Override
    public void setProvider(FeatureProvider provider) {
        // No-op - silently ignore
    }

    @Override
    public void setProvider(String domain, FeatureProvider provider) {
        // No-op - silently ignore
    }

    @Override
    public void setProviderAndWait(FeatureProvider provider) throws OpenFeatureError {
        // No-op - silently ignore
    }

    @Override
    public void setProviderAndWait(String domain, FeatureProvider provider) throws OpenFeatureError {
        // No-op - silently ignore
    }

    @Override
    public FeatureProvider getProvider() {
        return NO_OP_PROVIDER;
    }

    @Override
    public FeatureProvider getProvider(String domain) {
        return NO_OP_PROVIDER;
    }

    @Override
    public ProviderMetadata getProviderMetadata() {
        return () -> "No-op Provider";
    }

    @Override
    public ProviderMetadata getProviderMetadata(String domain) {
        return getProviderMetadata();
    }

    @Override
    public void addHooks(Hook... hooks) {
        // No-op - silently ignore
    }

    @Override
    public List<Hook> getHooks() {
        return Collections.emptyList();
    }

    @Override
    public void clearHooks() {
        // No-op - nothing to clear
    }

    @Override
    public OpenFeatureAPI setEvaluationContext(EvaluationContext evaluationContext) {
        return this; // No-op - return self for chaining
    }

    @Override
    public EvaluationContext getEvaluationContext() {
        return EvaluationContext.EMPTY;
    }

    @Override
    public OpenFeatureAPI removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        return this;
    }

    @Override
    public TransactionContextPropagator getTransactionContextPropagator() {
        return NO_OP_TRANSACTION_CONTEXT_PROPAGATOR;
    }

    @Override
    public void setTransactionContextPropagator(TransactionContextPropagator transactionContextPropagator) {
        // No-op - silently ignore
    }

    @Override
    public void setTransactionContext(EvaluationContext evaluationContext) {
        // No-op - silently ignore
    }

    @Override
    public void shutdown() {
        // No-op - silently ignore
    }

    @Override
    public OpenFeatureAPI onProviderReady(Consumer<EventDetails> handler) {
        return this;
    }

    @Override
    public OpenFeatureAPI onProviderConfigurationChanged(Consumer<EventDetails> handler) {
        return this;
    }

    @Override
    public OpenFeatureAPI onProviderStale(Consumer<EventDetails> handler) {
        return this;
    }

    @Override
    public OpenFeatureAPI onProviderError(Consumer<EventDetails> handler) {
        return this;
    }

    @Override
    public OpenFeatureAPI on(ProviderEvent event, Consumer<EventDetails> handler) {
        return this;
    }
}
