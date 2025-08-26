package dev.openfeature.api;

import java.util.ServiceLoader;

/**
 * Main abstract class that combines all OpenFeature interfaces.
 * Uses ServiceLoader pattern to automatically discover and load implementations.
 * This allows for multiple SDK implementations with priority-based selection.
 */
public abstract class OpenFeatureAPI implements 
        OpenFeatureCore, 
        OpenFeatureHooks, 
        OpenFeatureContext {

    private static volatile OpenFeatureAPI instance;
    private static final Object lock = new Object();

    /**
     * Gets the singleton OpenFeature API instance.
     * Uses ServiceLoader to automatically discover and load the best available implementation.
     * 
     * @return The singleton instance
     */
    public static OpenFeatureAPI getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = loadImplementation();
                }
            }
        }
        return instance;
    }

    /**
     * Load the best available OpenFeature implementation using ServiceLoader.
     * Implementations are selected based on priority, with higher priorities taking precedence.
     * If no implementation is available, returns a no-op implementation.
     * 
     * @return the loaded OpenFeature API implementation
     */
    private static OpenFeatureAPI loadImplementation() {
        ServiceLoader<OpenFeatureAPIProvider> loader = 
            ServiceLoader.load(OpenFeatureAPIProvider.class);

        OpenFeatureAPIProvider bestProvider = null;
        int highestPriority = Integer.MIN_VALUE;

        for (OpenFeatureAPIProvider provider : loader) {
            try {
                int priority = provider.getPriority();
                if (priority > highestPriority) {
                    bestProvider = provider;
                    highestPriority = priority;
                }
            } catch (Exception e) {
                // Log but continue - don't let one bad provider break everything
                System.err.println("Failed to get priority from provider " + 
                    provider.getClass().getName() + ": " + e.getMessage());
            }
        }

        if (bestProvider != null) {
            try {
                return bestProvider.createAPI();
            } catch (Exception e) {
                System.err.println("Failed to create API from provider " + 
                    bestProvider.getClass().getName() + ": " + e.getMessage());
                // Fall through to no-op
            }
        }

        return new NoOpOpenFeatureAPI();
    }

    /**
     * Reset the singleton instance. This method is primarily for testing purposes
     * and should be used with caution in production environments.
     */
    protected static void resetInstance() {
        synchronized (lock) {
            instance = null;
        }
    }

    // All methods from the implemented interfaces are abstract and must be implemented by concrete classes
}