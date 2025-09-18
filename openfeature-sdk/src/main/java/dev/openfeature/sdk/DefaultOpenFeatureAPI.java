package dev.openfeature.sdk;

import dev.openfeature.api.Client;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.EventDetails;
import dev.openfeature.api.FeatureProvider;
import dev.openfeature.api.Hook;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.ProviderEvent;
import dev.openfeature.api.ProviderEventDetails;
import dev.openfeature.api.ProviderMetadata;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.TransactionContextPropagator;
import dev.openfeature.api.exceptions.OpenFeatureError;
import dev.openfeature.api.internal.noop.NoOpTransactionContextPropagator;
import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of OpenFeature API that provides full SDK functionality.
 * This implementation extends the abstract API and provides all OpenFeature capabilities including
 * provider management, event handling, transaction context management, and lifecycle management.
 * Package-private - users should access this through OpenFeatureAPI.getInstance().
 */
@SuppressWarnings("PMD.UnusedLocalVariable")
class DefaultOpenFeatureAPI extends OpenFeatureAPI {
    private static final Logger log = LoggerFactory.getLogger(DefaultOpenFeatureAPI.class);
    // package-private multi-read/single-write lock
    static AutoCloseableReentrantReadWriteLock lock = new AutoCloseableReentrantReadWriteLock();
    private final ConcurrentLinkedQueue<Hook> apiHooks;
    private ProviderRepository providerRepository;
    private EventSupport eventSupport;
    private final AtomicReference<EvaluationContext> evaluationContext = new AtomicReference<>();
    private TransactionContextPropagator transactionContextPropagator;

    /**
     * Creates a new DefaultOpenFeatureAPI instance with default settings.
     * Initializes the API with empty hooks, a provider repository, event support,
     * and a no-op transaction context propagator.
     * Package-private constructor - this class should only be instantiated by the SDK.
     */
    DefaultOpenFeatureAPI() {
        apiHooks = new ConcurrentLinkedQueue<>();
        providerRepository = new ProviderRepository(this);
        eventSupport = new EventSupport();
        transactionContextPropagator = new NoOpTransactionContextPropagator();
    }

    /**
     * Get metadata about the default provider.
     *
     * @return the provider metadata
     */
    @Override
    public ProviderMetadata getProviderMetadata() {
        return getProvider().getMetadata();
    }

    /**
     * Get metadata about a registered provider using the client name.
     * An unbound or empty client name will return metadata from the default provider.
     *
     * @param domain an identifier which logically binds clients with providers
     * @return the provider metadata
     */
    @Override
    public ProviderMetadata getProviderMetadata(String domain) {
        return getProvider(domain).getMetadata();
    }

    /**
     * A factory function for creating new, OpenFeature client.
     * Clients can contain their own state (e.g. logger, hook, context).
     * Multiple clients can be used to segment feature flag configuration.
     * All un-named or unbound clients use the default provider.
     *
     * @return a new client instance
     */
    @Override
    public Client getClient() {
        return getClient(null, null);
    }

    /**
     * A factory function for creating new domainless OpenFeature client.
     * Clients can contain their own state (e.g. logger, hook, context).
     * Multiple clients can be used to segment feature flag configuration.
     * If there is already a provider bound to this domain, this provider will be used.
     * Otherwise, the default provider is used until a provider is assigned to that domain.
     *
     * @param domain an identifier which logically binds clients with providers
     * @return a new client instance
     */
    @Override
    public Client getClient(String domain) {
        return getClient(domain, null);
    }

    /**
     * A factory function for creating new domainless OpenFeature client.
     * Clients can contain their own state (e.g. logger, hook, context).
     * Multiple clients can be used to segment feature flag configuration.
     * If there is already a provider bound to this domain, this provider will be used.
     * Otherwise, the default provider is used until a provider is assigned to that domain.
     *
     * @param domain  a identifier which logically binds clients with providers
     * @param version a version identifier
     * @return a new client instance
     */
    @Override
    public Client getClient(String domain, String version) {
        return new OpenFeatureClient(this, domain, version);
    }

    /**
     * Sets the global evaluation context, which will be used for all evaluations.
     *
     * @param evaluationContext the context
     * @return api instance
     */
    @Override
    public dev.openfeature.api.OpenFeatureAPI setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContext.set(evaluationContext);
        return this;
    }

    /**
     * Gets the global evaluation context, which will be used for all evaluations.
     *
     * @return evaluation context
     */
    @Override
    public EvaluationContext getEvaluationContext() {
        return evaluationContext.get();
    }

    /**
     * Return the transaction context propagator.
     */
    @Override
    public TransactionContextPropagator getTransactionContextPropagator() {
        try (AutoCloseableLock ignored = lock.readLockAutoCloseable()) {
            return this.transactionContextPropagator;
        }
    }

    /**
     * Sets the transaction context propagator.
     *
     * @throws IllegalArgumentException if {@code transactionContextPropagator} is null
     */
    @Override
    public void setTransactionContextPropagator(TransactionContextPropagator transactionContextPropagator) {
        if (transactionContextPropagator == null) {
            throw new IllegalArgumentException("Transaction context propagator cannot be null");
        }
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
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
    @Override
    public void setProvider(FeatureProvider provider) {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
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
     * @param domain   The domain to bind the provider to.
     * @param provider The provider to set.
     */
    @Override
    public void setProvider(String domain, FeatureProvider provider) {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(
                    domain,
                    provider,
                    this::attachEventProvider,
                    this::emitReady,
                    this::detachEventProvider,
                    this::emitError,
                    false);
        }
    }

    /**
     * Sets the default provider and waits for its initialization to complete.
     *
     * <p>Note: If the provider fails during initialization, an {@link OpenFeatureError} will be thrown.
     * It is recommended to wrap this call in a try-catch block to handle potential initialization failures gracefully.
     *
     * @param provider the {@link FeatureProvider} to set as the default.
     * @throws OpenFeatureError if the provider fails during initialization.
     */
    public void setProviderAndWait(FeatureProvider provider) throws OpenFeatureError {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
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
     * <p>Note: If the provider fails during initialization, an {@link OpenFeatureError} will be thrown.
     * It is recommended to wrap this call in a try-catch block to handle potential initialization failures gracefully.
     *
     * @param domain   The domain to bind the provider to.
     * @param provider The provider to set.
     * @throws OpenFeatureError if the provider fails during initialization.
     */
    public void setProviderAndWait(String domain, FeatureProvider provider) throws OpenFeatureError {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            providerRepository.setProvider(
                    domain,
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
            ((EventProvider) provider).attach(this::runHandlersForProvider);
        }
    }

    private void emitReady(FeatureProvider provider) {
        runHandlersForProvider(provider, ProviderEvent.PROVIDER_READY, ProviderEventDetails.EMPTY);
    }

    private void detachEventProvider(FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            ((EventProvider) provider).detach();
        }
    }

    private void emitError(FeatureProvider provider, OpenFeatureError exception) {
        runHandlersForProvider(provider, ProviderEvent.PROVIDER_ERROR, ProviderEventDetails.of(exception.getMessage()));
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
     * Adds hooks for globally, used for all evaluations.
     * Hooks are run in the order they're added in the before stage. They are run in reverse order for all other stages.
     *
     * @param hooks The hook to add.
     */
    @Override
    public void addHooks(Hook... hooks) {
        this.apiHooks.addAll(Arrays.asList(hooks));
    }

    /**
     * Fetch the hooks associated to this client.
     *
     * @return A list of {@link Hook}s.
     */
    @Override
    public List<Hook> getHooks() {
        return new ArrayList<>(this.apiHooks);
    }

    /**
     * Returns a reference to the collection of {@link Hook}s.
     *
     * @return The collection of {@link Hook}s.
     */
    public Collection<Hook> getMutableHooks() {
        return this.apiHooks;
    }

    /**
     * Removes all hooks.
     */
    @Override
    public void clearHooks() {
        this.apiHooks.clear();
    }

    /**
     * Shut down and reset the current status of OpenFeature API.
     * This call cleans up all active providers and attempts to shut down internal
     * event handling mechanisms.
     * Once shut down is complete, API is reset and ready to use again.
     */
    @Override
    public void shutdown() {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            providerRepository.shutdown();
            eventSupport.shutdown();

            providerRepository = new ProviderRepository(this);
            eventSupport = new EventSupport();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public dev.openfeature.api.OpenFeatureAPI onProviderReady(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_READY, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public dev.openfeature.api.OpenFeatureAPI onProviderConfigurationChanged(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public dev.openfeature.api.OpenFeatureAPI onProviderStale(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_STALE, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public dev.openfeature.api.OpenFeatureAPI onProviderError(Consumer<EventDetails> handler) {
        return this.on(ProviderEvent.PROVIDER_ERROR, handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public dev.openfeature.api.OpenFeatureAPI on(ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            this.eventSupport.addGlobalHandler(event, handler);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public dev.openfeature.api.OpenFeatureAPI removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            this.eventSupport.removeGlobalHandler(event, handler);
        }
        return this;
    }

    void removeHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            eventSupport.removeClientHandler(domain, event, handler);
        }
    }

    void addHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            // if the provider is in the state associated with event, run immediately
            if (Optional.ofNullable(this.providerRepository.getProviderState(domain))
                    .orElse(ProviderState.READY)
                    .matchesEvent(event)) {
                eventSupport.runHandler(
                        handler,
                        EventDetails.of(getProvider(domain).getMetadata().getName(), domain));
            }
            eventSupport.addClientHandler(domain, event, handler);
        }
    }

    /**
     * Get the feature provider state manager for a domain.
     * Package-private method used by SDK implementations.
     */
    public FeatureProviderStateManager getFeatureProviderStateManager(String domain) {
        return providerRepository.getFeatureProviderStateManager(domain);
    }

    /**
     * Runs the handlers associated with a particular provider.
     *
     * @param provider the provider from where this event originated
     * @param event    the event type
     * @param details  the event details
     */
    private void runHandlersForProvider(FeatureProvider provider, ProviderEvent event, ProviderEventDetails details) {
        try (AutoCloseableLock ignored = lock.readLockAutoCloseable()) {

            List<String> domainsForProvider = providerRepository.getDomainsForProvider(provider);

            final String providerName = Optional.ofNullable(provider.getMetadata())
                    .map(ProviderMetadata::getName)
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .orElse("unknown");

            // run the global handlers
            eventSupport.runGlobalHandlers(event, EventDetails.of(providerName, details));

            // run the handlers associated with domains for this provider
            domainsForProvider.forEach(domain ->
                    eventSupport.runClientHandlers(domain, event, EventDetails.of(providerName, domain, details)));

            if (providerRepository.isDefaultProvider(provider)) {
                // run handlers for clients that have no bound providers (since this is the default)
                Set<String> allDomainNames = eventSupport.getAllDomainNames();
                Set<String> boundDomains = providerRepository.getAllBoundDomains();
                allDomainNames.removeAll(boundDomains);
                allDomainNames.forEach(domain ->
                        eventSupport.runClientHandlers(domain, event, EventDetails.of(providerName, domain, details)));
            }
        }
    }
}
