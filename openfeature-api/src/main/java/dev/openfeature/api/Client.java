package dev.openfeature.api;

import dev.openfeature.api.evaluation.EvaluationClient;
import dev.openfeature.api.evaluation.EvaluationContextHolder;
import dev.openfeature.api.events.EventBus;
import dev.openfeature.api.lifecycle.Hookable;
import dev.openfeature.api.tracking.Tracking;
import dev.openfeature.api.types.ClientMetadata;

/**
 * Interface used to resolve flags of varying types.
 */
public interface Client
        extends EvaluationClient, Tracking, EventBus<Client>, Hookable<Client>, EvaluationContextHolder<Client> {
    ClientMetadata getMetadata();

    /**
     * Returns the current state of the associated provider.
     *
     * @return the provider state
     */
    ProviderState getProviderState();
}
