package dev.openfeature.sdk;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import dev.openfeature.sdk.internal.*;
import lombok.extern.slf4j.Slf4j;

/**
 * A global singleton which holds base configuration for the OpenFeature library.
 * Configuration here will be shared across all {@link Client}s.
 */
@Slf4j
public class OpenFeatureAPI {
    // package-private multi-read/single-write lock
    static AutoCloseableReentrantReadWriteLock hooksLock = new AutoCloseableReentrantReadWriteLock();
    static AutoCloseableReentrantReadWriteLock contextLock = new AutoCloseableReentrantReadWriteLock();

    private final List<Hook> apiHooks;
    private final Map<String, FeatureProvider> providers = new ConcurrentHashMap<>();
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private final AtomicReference<FeatureProvider> initializingDefaultProvider = new AtomicReference<>();
    private final Map<String, FeatureProvider> initializingNamedProviders = new ConcurrentHashMap<>();

    private FeatureProvider defaultProvider = new NoOpProvider();
    private EvaluationContext evaluationContext;

    private OpenFeatureAPI() {
        this.apiHooks = new ArrayList<>();
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
        return defaultProvider.getMetadata();
    }

    public Metadata getProviderMetadata(String clientName) {
        return getProvider(clientName).getMetadata();
    }

    public Client getClient() {
        return getClient(null, null);
    }

    public Client getClient(@Nullable String name) {
        return getClient(name, null);
    }

    public Client getClient(@Nullable String name, @Nullable String version) {
        return new OpenFeatureClient(this, name, version);
    }

    /**
     * {@inheritDoc}
     */
    public void setEvaluationContext(EvaluationContext evaluationContext) {
        try (AutoCloseableLock __ = contextLock.writeLockAutoCloseable()) {
            this.evaluationContext = evaluationContext;
        }
    }

    /**
     * {@inheritDoc}
     */
    public EvaluationContext getEvaluationContext() {
        try (AutoCloseableLock __ = contextLock.readLockAutoCloseable()) {
            return this.evaluationContext;
        }
    }

    /**
     * Set the default provider.
     */
    public void setProvider(FeatureProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        shutdownProvider(this.defaultProvider);
        initializeProvider(provider);
    }

    /**
     * Add a provider for a named client.
     * @param clientName The name of the client.
     * @param provider The provider to set.
     */
    public void setProvider(String clientName, FeatureProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        shutdownProvider(clientName);
        initializeProvider(clientName, provider);
    }

    private void shutdownProvider(FeatureProvider provider) {
        if (provider == null) {
            return;
        }
        taskExecutor.submit(() -> {
            try {
                provider.shutdown();
            } catch (Exception e) {
                log.error("Exception when shutting down feature provider {}", provider.getClass().getName(), e);
            }
        });
    }

    private void shutdownProvider(String clientName) {
        shutdownProvider(providers.get(clientName));
    }

    private void initializeProvider(FeatureProvider provider) {
        initializingDefaultProvider.set(provider);
        initializeProvider(provider,
            newProvider -> Optional
                .ofNullable(initializingDefaultProvider.get())
                .filter(initializingProvider -> initializingProvider == newProvider)
                .ifPresent(initializedProvider -> defaultProvider = initializedProvider));
    }

    private void initializeProvider(String clientName, FeatureProvider provider) {
        initializingNamedProviders.put(clientName, provider);
        initializeProvider(provider, newProvider -> Optional
            .ofNullable(initializingNamedProviders.get(clientName))
            .filter(initializingProvider -> initializingProvider == newProvider)
            .ifPresent(initializedProvider -> this.providers.put(clientName, initializedProvider)));
    }

    private void initializeProvider(FeatureProvider provider, Consumer<FeatureProvider> afterInitialization) {
        taskExecutor.submit(() -> {
            try {
                provider.initialize();
                afterInitialization.accept(provider);
            } catch (Exception e) {
                log.error("Exception when initializing feature provider {}", provider.getClass().getName(), e);
            }
        });
    }

    /**
     * Return the default provider.
     */
    public FeatureProvider getProvider() {
        return defaultProvider;
    }

    /**
     * Fetch a provider for a named client. If not found, return the default.
     * @param name The client name to look for.
     * @return A named {@link FeatureProvider}
     */
    public FeatureProvider getProvider(String name) {
        return Optional.ofNullable(name).map(this.providers::get).orElse(defaultProvider);
    }

    /**
     * {@inheritDoc}
     */
    public void addHooks(Hook... hooks) {
        try (AutoCloseableLock __ = hooksLock.writeLockAutoCloseable()) {
            this.apiHooks.addAll(Arrays.asList(hooks));
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Hook> getHooks() {
        try (AutoCloseableLock __ = hooksLock.readLockAutoCloseable()) {
            return this.apiHooks;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearHooks() {
        try (AutoCloseableLock __ = hooksLock.writeLockAutoCloseable()) {
            this.apiHooks.clear();
        }
    }
}
