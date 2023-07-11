package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

/**
 * A global singleton which holds base configuration for the OpenFeature
 * library.
 * Configuration here will be shared across all {@link Client}s.
 */
@Slf4j
public class OpenFeatureAPI implements EventBus<OpenFeatureAPI> {
    // package-private multi-read/single-write lock
    static AutoCloseableReentrantReadWriteLock lock = new AutoCloseableReentrantReadWriteLock();
    private EvaluationContext evaluationContext;
    private final List<Hook> apiHooks;
    private ProviderRepository providerRepository = new ProviderRepository();
    private EventSupport eventSupport = new EventSupport();

    protected OpenFeatureAPI() {
        apiHooks = new ArrayList<>();
    }

    private static class SingletonHolder {
        private static final OpenFeatureAPI INSTANCE = new OpenFeatureAPI();
    }

    /**
     * Provisions the {@link OpenFeatureAPI} singleton (if needed) and returns it.
     *
     * @return The singleton instance.
     */
    public static OpenFeatureAPI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Metadata getProviderMetadata() {
        return getProvider().getMetadata();
    }

    public Metadata getProviderMetadata(String clientName) {
        return getProvider(clientName).getMetadata();
    }

    /**
     * {@inheritDoc}
     */
    public Client getClient() {
        return getClient(null, null);
    }

    /**
     * {@inheritDoc}
     */
    public Client getClient(@Nullable String name) {
        return getClient(name, null);
    }

    /**
     * {@inheritDoc}
     */
    public Client getClient(@Nullable String name, @Nullable String version) {
        return new OpenFeatureClient(this,
                name,
                version);
    }

    /**
     * {@inheritDoc}
     */
    public void setEvaluationContext(EvaluationContext evaluationContext) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            this.evaluationContext = evaluationContext;
        }
    }

    /**
     * {@inheritDoc}
     */
    public EvaluationContext getEvaluationContext() {
        try (AutoCloseableLock __ = lock.readLockAutoCloseable()) {
            return this.evaluationContext;
        }
    }

    /**
     * Set the default provider.
     */
    public void setProvider(FeatureProvider provider) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(
                    provider,
                    (p) -> attachEventProvider(p),
                    (p) -> emitReady(p),
                    (p) -> detachEventProvider(p),
                    (p, message) -> emitError(p, message));
        }
    }

    /**
     * Add a provider for a named client.
     *
     * @param clientName The name of the client.
     * @param provider   The provider to set.
     */
    public void setProvider(String clientName, FeatureProvider provider) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(clientName,
                    provider,
                    this::attachEventProvider,
                    this::emitReady,
                    this::detachEventProvider,
                    this::emitError);
        }
    }

    private void attachEventProvider(FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            ((EventProvider)provider).attach((p, event, details) -> {
                runHandlersForProvider(p, event, details);
            });
        }
    }

    private void emitReady(FeatureProvider provider) {
        runHandlersForProvider(provider, ProviderEvent.PROVIDER_READY, ProviderEventDetails.builder().build());
    }

    private void detachEventProvider(FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            ((EventProvider)provider).detach();
        }
    }

    private void emitError(FeatureProvider provider, String message) {
        runHandlersForProvider(provider, ProviderEvent.PROVIDER_ERROR,
                ProviderEventDetails.builder().message(message).build());
    }

    /**
     * Return the default provider.
     */
    public FeatureProvider getProvider() {
        return providerRepository.getProvider();
    }

    /**
     * Fetch a provider for a named client. If not found, return the default.
     *
     * @param name The client name to look for.
     * @return A named {@link FeatureProvider}
     */
    public FeatureProvider getProvider(String name) {
        return providerRepository.getProvider(name);
    }

    /**
     * {@inheritDoc}
     */
    public void addHooks(Hook... hooks) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            this.apiHooks.addAll(Arrays.asList(hooks));
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Hook> getHooks() {
        try (AutoCloseableLock __ = lock.readLockAutoCloseable()) {
            return this.apiHooks;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearHooks() {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            this.apiHooks.clear();
        }
    }

    public void shutdown() {
        providerRepository.shutdown();
        eventSupport.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureAPI onProviderReady(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_READY, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureAPI onProviderConfigurationChanged(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureAPI onProviderStale(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_STALE, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureAPI onProviderError(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_ERROR, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureAPI on(ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            this.eventSupport.addGlobalHandler(event, handler);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureAPI removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        this.eventSupport.removeGlobalHandler(event, handler);
        return this;
    }

    void removeHandler(String clientName, ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            eventSupport.removeClientHandler(clientName, event, handler);
        }
    }

    void addHandler(String clientName, ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            // if the provider is READY, run immediately
            if (ProviderEvent.PROVIDER_READY.equals(event)
                    && ProviderState.READY.equals(this.providerRepository.getProvider(clientName).getState())) {
                eventSupport.runHandler(handler, EventDetails.builder().clientName(clientName).build());
            }
            eventSupport.addClientHandler(clientName, event, handler);
        }
    }

    /**
     * This method is only here for testing as otherwise all tests after the API
     * shutdown test would fail.
     */
    final void reset() {
        providerRepository = new ProviderRepository();
        eventSupport = new EventSupport();
    }

    /**
     * Runs the handlers associated with a particular provider.
     * 
     * @param provider the provider from where this event originated
     * @param event the event type
     * @param details the event details
     */
    private void runHandlersForProvider(FeatureProvider provider, ProviderEvent event, ProviderEventDetails details) {
        try (AutoCloseableLock __ = lock.readLockAutoCloseable()) {
    
            List<String> clientNamesForProvider = providerRepository
                .getClientNamesForProvider(provider);
    
            // run the global handlers
            eventSupport.runGlobalHandlers(event, EventDetails.fromProviderEventDetails(details));

            // run the handlers associated with named clients for this provider
            clientNamesForProvider.forEach(name -> {   
                eventSupport.runClientHandlers(name, event, EventDetails.fromProviderEventDetails(details, name));
            });
    
            if (providerRepository.isDefaultProvider(provider)) {
                // run handlers for clients that have no bound providers (since this is the default)
                Set<String> allClientNames = eventSupport.getAllClientNames();
                Set<String> boundClientNames = providerRepository.getAllBoundClientNames();
                allClientNames.removeAll(boundClientNames);
                allClientNames.forEach(name -> {
                    eventSupport.runClientHandlers(name, event, EventDetails.fromProviderEventDetails(details, name));
                });
            }
        }
    }
}
