package dev.openfeature.api;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * No-operation implementation of OpenFeatureAPI that provides safe defaults.
 * Used as a fallback when no actual implementation is available via ServiceLoader.
 * All operations are safe no-ops that won't affect application functionality.
 */
public class NoOpOpenFeatureAPI extends OpenFeatureAPI {
    
    private static final NoOpClient NO_OP_CLIENT = new NoOpClient();

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
    public Metadata getProviderMetadata() {
        return () -> "No-op Provider";
    }

    @Override
    public Metadata getProviderMetadata(String domain) {
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

    // Implementation of OpenFeatureEventHandling interface
    
    @Override
    public void addHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        // No-op - silently ignore
    }
    
    @Override
    public void removeHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        // No-op - silently ignore
    }
    
}