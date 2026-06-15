package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds base configuration for the OpenFeature library.
 *
 * <p>Most applications should use the global singleton via {@link #getInstance()}; configuration
 * there is shared across all {@link Client}s. For dependency-injection frameworks, testing, or
 * multi-tenant scenarios that need fully independent state (providers, hooks, evaluation context,
 * event handlers, transaction context propagators), create isolated instances via
 * {@link #createIsolated()}.
 *
 * <p><strong>Note:</strong> Isolated API instances (per spec section 1.8) are experimental and
 * subject to change.
 *
 * @see <a href="https://openfeature.dev/specification/sections/flag-evaluation#18-isolated-api-instances">
 *     Spec &sect;1.8 &mdash; Isolated API Instances</a>
 */
@Slf4j
@SuppressWarnings("PMD.UnusedLocalVariable")
public class OpenFeatureAPI implements EventBus<OpenFeatureAPI> {

    /**
     * Global registry tracking which API instance each provider is currently bound to.
     * Used to detect violations of spec requirement 1.8.4 (a provider SHOULD NOT be
     * registered with more than one API instance simultaneously).
     *
     * <p>Backed by a {@link WeakHashMap} so providers that are discarded without explicit
     * deregistration do not pin the API instance in memory.
     */
    private static final Map<FeatureProvider, OpenFeatureAPI> GLOBAL_PROVIDER_REGISTRY =
            Collections.synchronizedMap(new WeakHashMap<>());

    // package-private multi-read/single-write lock (instance-level for isolation)
    final AutoCloseableReentrantReadWriteLock lock;
    private final ConcurrentLinkedQueue<Hook> apiHooks;
    private ProviderRepository providerRepository;
    private EventSupport eventSupport;
    private final AtomicReference<EvaluationContext> evaluationContext = new AtomicReference<>();
    private TransactionContextPropagator transactionContextPropagator;

    /**
     * Creates and returns a new, independent {@link OpenFeatureAPI} instance with fully isolated
     * state (providers, hooks, evaluation context, event handlers, transaction context
     * propagators).
     *
     * @apiNote This API is experimental and subject to change.
     * @see <a href="https://openfeature.dev/specification/sections/flag-evaluation#18-isolated-api-instances">
     *     Spec &sect;1.8 &mdash; Isolated API Instances</a>
     */
    public static OpenFeatureAPI createIsolated() {
        return new OpenFeatureAPI();
    }

    // Package-private: not part of the public API; use createIsolated().
    OpenFeatureAPI() {
        this(new AutoCloseableReentrantReadWriteLock());
    }

    // Package-private constructor for testing with a custom lock.
    OpenFeatureAPI(AutoCloseableReentrantReadWriteLock lock) {
        this.lock = lock;
        apiHooks = new ConcurrentLinkedQueue<>();
        providerRepository = new ProviderRepository(this);
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

    /**
     * Get metadata about the default provider.
     *
     * @return the provider metadata
     */
    public Metadata getProviderMetadata() {
        return getProvider().getMetadata();
    }

    /**
     * Get metadata about a registered provider using the client name.
     * An unbound or empty client name will return metadata from the default provider.
     *
     * @param domain an identifier which logically binds clients with providers
     * @return the provider metadata
     */
    public Metadata getProviderMetadata(String domain) {
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
    public Client getClient(String domain, String version) {
        return new OpenFeatureClient(this, domain, version);
    }

    /**
     * Sets the global evaluation context, which will be used for all evaluations.
     *
     * @param evaluationContext the context
     * @return api instance
     */
    public OpenFeatureAPI setEvaluationContext(EvaluationContext evaluationContext) {
        this.evaluationContext.set(evaluationContext);
        return this;
    }

    /**
     * Gets the global evaluation context, which will be used for all evaluations.
     *
     * @return evaluation context
     */
    public EvaluationContext getEvaluationContext() {
        return evaluationContext.get();
    }

    /**
     * Return the transaction context propagator.
     */
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
            ((EventProvider) provider).attach(this::runHandlersForProvider, this.lock);
        }
    }

    private void emitReady(FeatureProvider provider) {
        runHandlersForProvider(
                provider,
                ProviderEvent.PROVIDER_READY,
                ProviderEventDetails.builder().build());
    }

    private void detachEventProvider(FeatureProvider provider) {
        if (provider instanceof EventProvider) {
            ((EventProvider) provider).detach();
        }
    }

    private void emitError(FeatureProvider provider, OpenFeatureError exception) {
        runHandlersForProvider(
                provider,
                ProviderEvent.PROVIDER_ERROR,
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
     * Adds hooks for globally, used for all evaluations.
     * Hooks are run in the order they're added in the before stage. They are run in reverse order for all other stages.
     *
     * @param hooks The hook to add.
     */
    public void addHooks(Hook... hooks) {
        this.apiHooks.addAll(Arrays.asList(hooks));
    }

    /**
     * Fetch the hooks associated to this client.
     *
     * @return A list of {@link Hook}s.
     */
    public List<Hook> getHooks() {
        return new ArrayList<>(this.apiHooks);
    }

    /**
     * Returns a reference to the collection of {@link Hook}s.
     *
     * @return The collection of {@link Hook}s.
     */
    Collection<Hook> getMutableHooks() {
        return this.apiHooks;
    }

    /**
     * Removes all hooks.
     */
    public void clearHooks() {
        this.apiHooks.clear();
    }

    /**
     * Registers a provider with the global registry, throwing if it is already
     * bound to a different API instance (spec requirement 1.8.4).
     *
     * @throws IllegalStateException if the provider is already bound to a different API instance
     */
    void registerGlobalProvider(FeatureProvider provider) {
        synchronized (GLOBAL_PROVIDER_REGISTRY) {
            OpenFeatureAPI existing = GLOBAL_PROVIDER_REGISTRY.get(provider);
            if (existing != null && existing != this) {
                throw new IllegalStateException("Provider "
                        + provider.getClass().getName()
                        + " is already registered with another API instance. "
                        + "A provider SHOULD NOT be bound to more than one API instance "
                        + "simultaneously (spec requirement 1.8.4).");
            }
            GLOBAL_PROVIDER_REGISTRY.put(provider, this);
        }
    }

    /**
     * Removes the provider from the global registry if this instance is the current owner.
     */
    void deregisterGlobalProvider(FeatureProvider provider) {
        GLOBAL_PROVIDER_REGISTRY.remove(provider, this);
    }

    /**
     * Shut down and reset the current status of OpenFeature API.
     * This call cleans up all active providers and attempts to shut down internal
     * event handling mechanisms.
     * Once shut down is complete, API is reset and ready to use again.
     */
    public void shutdown() {
        List<FeatureProviderStateManager> managersToShutdown;
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            // Mark repository as shutting down while holding lock.
            // This ensures setProvider calls will throw IllegalStateException.
            managersToShutdown = providerRepository.prepareShutdown();
        }

        if (managersToShutdown != null) {
            // Complete shutdown without holding lock to avoid deadlock.
            // Pending tasks (e.g., initializeProvider) may need the read lock to emit events.
            providerRepository.completeShutdown(managersToShutdown);
            eventSupport.shutdown();

            try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
                providerRepository = new ProviderRepository(this);
                eventSupport = new EventSupport();
                clearHooks();
                setEvaluationContext(ImmutableContext.EMPTY);
                setTransactionContextPropagator(new NoOpTransactionContextPropagator());
            }
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
        try (AutoCloseableLock ignored = lock.writeLockAutoCloseable()) {
            this.eventSupport.addGlobalHandler(event, handler);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OpenFeatureAPI removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
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
                        handler, EventDetails.builder().domain(domain).build());
            }
            eventSupport.addClientHandler(domain, event, handler);
        }
    }

    FeatureProviderStateManager getFeatureProviderStateManager(String domain) {
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
                    .map(Metadata::getName)
                    .orElse(null);

            // run the global handlers
            eventSupport.runGlobalHandlers(event, EventDetails.fromProviderEventDetails(details, providerName));

            // run the handlers associated with domains for this provider
            domainsForProvider.forEach(domain -> eventSupport.runClientHandlers(
                    domain, event, EventDetails.fromProviderEventDetails(details, providerName, domain)));

            if (providerRepository.isDefaultProvider(provider)) {
                // run handlers for clients that have no bound providers (since this is the default)
                Set<String> allDomainNames = eventSupport.getAllDomainNames();
                Set<String> boundDomains = providerRepository.getAllBoundDomains();
                allDomainNames.removeAll(boundDomains);
                allDomainNames.forEach(domain -> eventSupport.runClientHandlers(
                        domain, event, EventDetails.fromProviderEventDetails(details, providerName, domain)));
            }
        }
    }
}
