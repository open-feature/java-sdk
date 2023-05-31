package dev.openfeature.sdk;

import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

    private AtomicReference<FeatureProvider> defaultProvider = new AtomicReference<>(new NoOpProvider());
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
        return defaultProvider.get().getMetadata();
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
        initializeProvider(provider);
    }

    /**
     * Add a provider for a named client.
     *
     * @param clientName The name of the client.
     * @param provider   The provider to set.
     */
    public void setProvider(String clientName, FeatureProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
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

    private void initializeProvider(FeatureProvider provider) {
        initializingDefaultProvider.set(provider);
        initializeProvider(provider, this::updateDefaultProviderAfterInitialization);
    }

    private void initializeProvider(String clientName, FeatureProvider provider) {
        initializingNamedProviders.put(clientName, provider);
        initializeProvider(provider, newProvider -> updateProviderAfterInitialization(clientName, newProvider));
    }

    private void updateProviderAfterInitialization(String clientName, FeatureProvider newProvider) {
        Optional
                .ofNullable(initializingNamedProviders.get(clientName))
                .filter(initializingProvider -> initializingProvider == newProvider)
                .ifPresent(provider -> updateNamedProviderAfterInitialization(clientName, provider));
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

    private void updateDefaultProviderAfterInitialization(FeatureProvider initializedProvider) {
        Optional
                .ofNullable(initializingDefaultProvider.get())
                .filter(initializingProvider -> initializingProvider == initializedProvider)
                .ifPresent(provider -> {
                    FeatureProvider oldProvider = this.defaultProvider.getAndSet(provider);
                    shutdownProvider(oldProvider);
                });
    }

    private void updateNamedProviderAfterInitialization(String clientName, FeatureProvider initializedProvider) {
        Optional
                .ofNullable(initializingNamedProviders.get(clientName))
                .filter(initializingProvider -> initializingProvider == initializedProvider)
                .ifPresent(provider -> {
                    FeatureProvider oldProvider = this.providers.put(clientName, provider);
                    shutdownProvider(oldProvider);
                });
    }

    /**
     * Return the default provider.
     */
    public FeatureProvider getProvider() {
        return defaultProvider.get();
    }

    /**
     * Fetch a provider for a named client. If not found, return the default.
     *
     * @param name The client name to look for.
     * @return A named {@link FeatureProvider}
     */
    public FeatureProvider getProvider(String name) {
        return Optional.ofNullable(name).map(this.providers::get).orElse(defaultProvider.get());
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
