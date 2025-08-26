package dev.openfeature.api;

/**
 * Interface for OpenFeature API lifecycle management operations.
 * Provides cleanup and shutdown capabilities for proper resource management.
 */
public interface OpenFeatureLifecycle {
    /**
     * Shut down and reset the current status of OpenFeature API.
     * This call cleans up all active providers and attempts to shut down internal
     * event handling mechanisms.
     * Once shut down is complete, API is reset and ready to use again.
     */
    void shutdown();
}
