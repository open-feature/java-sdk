package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A global singleton which holds base configuration for the OpenFeature
 * library.
 * Configuration here will be shared across all {@link Client}s.
 */
@Slf4j
@SuppressWarnings("PMD.UnusedLocalVariable")
public class OpenFeatureAPI implements EventBus<OpenFeatureAPI> {
    // package-private multi-read/single-write lock
    static AutoCloseableReentrantReadWriteLock lock = new AutoCloseableReentrantReadWriteLock();
    private final List<Hook> apiHooks;
    private ProviderRepository providerRepository;
    private EventSupport eventSupport;
    private EvaluationContext evaluationContext;
    private TransactionContextPropagator transactionContextPropagator;

    protected OpenFeatureAPI() {
        apiHooks = new ArrayList<>();
        providerRepository = new ProviderRepository();
        eventSupport = new EventSupport();
        transactionContextPropagator = new NoOpTransactionContextPropagator();
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

    public Metadata getProviderMetadata(String domain) {
        return getProvider(domain).getMetadata();
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
    public Client getClient(String domain) {
        return getClient(domain, null);
    }

    /**
     * {@inheritDoc}
     */
    public Client getClient(String domain, String version) {
        return new OpenFeatureClient(this,
                domain,
                version);
    }

    /**
     * {@inheritDoc}
     */
    public OpenFeatureAPI setEvaluationContext(EvaluationContext evaluationContext) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            this.evaluationContext = evaluationContext;
        }
        return this;
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
     * Return the transaction context propagator.
     */
    public TransactionContextPropagator getTransactionContextPropagator() {
        try (AutoCloseableLock __ = lock.readLockAutoCloseable()) {
            return this.transactionContextPropagator;
        }
    }

    /**
     * Sets the transaction context propagator.
     *
     * @throws IllegalArgumentException if {@code transactionContextPropagator} is null
     */
    public void setTransactionContextPropagator(TransactionContextPropagator transactionContextPropagator) {
        if (transactionContextPropagator == null) {
            throw new IllegalArgumentException("Transaction context propagator cannot be null");
        }
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            this.transactionContextPropagator = transactionContextPropagator;
        }
    }

    /**
     * Returns the currently defined transaction context using the registered transaction
     * context propagator.
     *
     * @return {@link EvaluationContext} The current transaction context
     */
    EvaluationContext getTransactionContext() {
        return this.transactionContextPropagator.getTransactionContext();
    }

    /**
     * Sets the transaction context using the registered transaction context propagator.
     */
    public void setTransactionContext(EvaluationContext evaluationContext) {
        this.transactionContextPropagator.setTransactionContext(evaluationContext);
    }

    /**
     * Set the default provider.
     */
    public void setProvider(FeatureProvider provider) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(
                    provider,
                    this::attachEventProvider,
                    this::emitReady,
                    this::detachEventProvider,
                    this::emitError,
                    false);
        }
    }

    /**
     * Add a provider for a domain.
     *
     * @param domain     The domain to bind the provider to.
     * @param provider   The provider to set.
     */
    public void setProvider(String domain, FeatureProvider provider) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(domain,
                    provider,
                    this::attachEventProvider,
                    this::emitReady,
                    this::detachEventProvider,
                    this::emitError,
                    false);
        }
    }

    /**
     * Set the default provider and wait for initialization to finish.
     */
    public void setProviderAndWait(FeatureProvider provider) throws OpenFeatureError {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(
                    provider,
                    this::attachEventProvider,
                    this::emitReady,
                    this::detachEventProvider,
                    this::emitErrorAndThrow,
                    true);
        }
    }

    /**
     * Add a provider for a domain and wait for initialization to finish.
     *
     * @param domain     The domain to bind the provider to.
     * @param provider   The provider to set.
     */
    public void setProviderAndWait(String domain, FeatureProvider provider) throws OpenFeatureError {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(domain,
                    provider,
                    this::attachEventProvider,
                    this::emitReady,
                    this::detachEventProvider,
                    this::emitErrorAndThrow,
                    true);
        }
    }

    private void attachEventProvider(FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            ((EventProvider) provider).attach((p, event, details) -> {
                runHandlersForProvider(p, event, details);
            });
        }
    }

    private void emitReady(FeatureProvider provider) {
        runHandlersForProvider(provider, ProviderEvent.PROVIDER_READY, ProviderEventDetails.builder().build());
    }

    private void detachEventProvider(FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            ((EventProvider) provider).detach();
        }
    }

    private void emitError(FeatureProvider provider, OpenFeatureError exception) {
        runHandlersForProvider(provider, ProviderEvent.PROVIDER_ERROR,
                ProviderEventDetails.builder().message(exception.getMessage()).build());
    }

    private void emitErrorAndThrow(FeatureProvider provider, OpenFeatureError exception) throws OpenFeatureError {
        this.emitError(provider, exception);
        throw exception;
    }

    /**
     * Return the default provider.
     */
    public FeatureProvider getProvider() {
        return providerRepository.getProvider();
    }

    /**
     * Fetch a provider for a domain. If not found, return the default.
     *
     * @param domain The domain to look for.
     * @return A named {@link FeatureProvider}
     */
    public FeatureProvider getProvider(String domain) {
        return providerRepository.getProvider(domain);
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

    /**
     * Shut down and reset the current status of OpenFeature API.
     * This call cleans up all active providers and attempts to shut down internal
     * event handling mechanisms.
     * Once shut down is complete, API is reset and ready to use again.
     */
    public void shutdown() {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            providerRepository.shutdown();
            eventSupport.shutdown();

            providerRepository = new ProviderRepository();
            eventSupport = new EventSupport();
        }
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

    void removeHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            eventSupport.removeClientHandler(domain, event, handler);
        }
    }

    void addHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock __ = lock.writeLockAutoCloseable()) {
            // if the provider is in the state associated with event, run immediately
            if (Optional.ofNullable(this.providerRepository.getProvider(domain).getState())
                    .orElse(ProviderState.READY).matchesEvent(event)) {
                eventSupport.runHandler(handler, EventDetails.builder().domain(domain).build());
            }
            eventSupport.addClientHandler(domain, event, handler);
        }
    }

    /**
     * Runs the handlers associated with a particular provider.
     * 
     * @param provider the provider from where this event originated
     * @param event    the event type
     * @param details  the event details
     */
    private void runHandlersForProvider(FeatureProvider provider, ProviderEvent event, ProviderEventDetails details) {
        try (AutoCloseableLock __ = lock.readLockAutoCloseable()) {

            List<String> domainsForProvider = providerRepository
                    .getDomainsForProvider(provider);

            final String providerName = Optional.ofNullable(provider.getMetadata())
                    .map(metadata -> metadata.getName())
                    .orElse(null);

            // run the global handlers
            eventSupport.runGlobalHandlers(event, EventDetails.fromProviderEventDetails(details, providerName));

            // run the handlers associated with domains for this provider
            domainsForProvider.forEach(domain -> {
                eventSupport.runClientHandlers(domain, event,
                        EventDetails.fromProviderEventDetails(details, providerName, domain));
            });

            if (providerRepository.isDefaultProvider(provider)) {
                // run handlers for clients that have no bound providers (since this is the default)
                Set<String> allDomainNames = eventSupport.getAllDomainNames();
                Set<String> boundDomains = providerRepository.getAllBoundDomains();
                allDomainNames.removeAll(boundDomains);
                allDomainNames.forEach(domain -> {
                    eventSupport.runClientHandlers(domain, event,
                            EventDetails.fromProviderEventDetails(details, providerName, domain));
                });
            }
        }
    }
}
