package dev.openfeature.sdk;

import java.util.List;

/**
 * Interface used to resolve flags of varying types.
 */
public interface Client extends Features, EventBus<Client> {
    Metadata getMetadata();

    /**
     * Return an optional client-level evaluation context.
     * @return {@link EvaluationContext}
     */
    EvaluationContext getEvaluationContext();

    /**
     * Set the client-level evaluation context.
     * @param ctx Client level context.
     */
    Client setEvaluationContext(EvaluationContext ctx);

    /**
     * Adds hooks for evaluation.
     * Hooks are run in the order they're added in the before stage. They are run in reverse order for all other stages.
     *
     * @param hooks The hook to add.
     */
    Client addHooks(Hook... hooks);

    /**
     * Fetch the hooks associated to this client.
     * @return A list of {@link Hook}s.
     */
    List<Hook> getHooks();
}
