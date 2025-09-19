package dev.openfeature.api.lifecycle;

import dev.openfeature.api.evaluation.EvaluationContext;

/**
 * Interface for lifecycle management operations.
 * Provides initialization and shutdown capabilities for proper resource management.
 */
public interface Lifecycle {
    /**
     * Shutdown and reset the current instance.
     * It is ok if the method is expensive as it is executed in the background. All
     * runtime exceptions will be
     * caught and logged.
     */
    void shutdown();

    /**
     * if needed can be used to call arbitrary code, which is not suited for the
     * constructor.
     */
    default void initialize(EvaluationContext context) throws Exception {}
}
