package dev.openfeature.sdk;

import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.OpenFeatureAPIProvider;

/**
 * ServiceLoader provider implementation for the default OpenFeature SDK.
 * This provider creates instances of the full-featured SDK implementation
 * with standard priority.
 */
public class DefaultOpenFeatureAPIProvider implements OpenFeatureAPIProvider {

    /**
     * Create an OpenFeature API implementation with full SDK functionality.
     * 
     * @return the default SDK implementation
     */
    @Override
    public OpenFeatureAPI createAPI() {
        return new DefaultOpenFeatureAPI();
    }

    /**
     * Standard priority for the default SDK implementation.
     * Other SDK implementations can use higher priorities to override this.
     * 
     * @return priority value (0 for standard implementation)
     */
    @Override
    public int getPriority() {
        return 0;
    }
}