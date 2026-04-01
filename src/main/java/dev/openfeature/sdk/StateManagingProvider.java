package dev.openfeature.sdk;

/**
 * A provider that manages its own state. The SDK reads state from the provider
 * rather than maintaining shadow state. Implementations MUST ensure that
 * {@link #getState()} is safe for concurrent access and that state transitions
 * and associated event emissions are atomic from the perspective of external observers.
 *
 * <p>Legacy providers that do not implement this interface continue to have their state
 * managed by the SDK (deprecated behavior, to be removed in the next major version).</p>
 *
 * @see FeatureProvider
 * @see EventProvider
 */
public interface StateManagingProvider extends FeatureProvider {

    /**
     * Returns the current state of this provider. Must reflect {@link ProviderState#NOT_READY}
     * before {@link #initialize(EvaluationContext)} is called and after {@link #shutdown()} completes.
     * Must reflect {@link ProviderState#READY} if {@link #initialize(EvaluationContext)} returns normally.
     *
     * <p>This method must be safe for concurrent access.</p>
     *
     * @return the current provider state
     */
    @Override
    ProviderState getState();
}
