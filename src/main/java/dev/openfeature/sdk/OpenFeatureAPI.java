package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;

/**
 * A global singleton which holds base configuration for the OpenFeature library.
 * Configuration here will be shared across all {@link Client}s.
 */
public class OpenFeatureAPI {
    // package-private multi-read/single-write lock
    static AutoCloseableReentrantReadWriteLock hooksLock = new AutoCloseableReentrantReadWriteLock();
    static AutoCloseableReentrantReadWriteLock providerLock = new AutoCloseableReentrantReadWriteLock();
    static AutoCloseableReentrantReadWriteLock contextLock = new AutoCloseableReentrantReadWriteLock();
    private EvaluationContext evaluationContext;
    private List<Hook> apiHooks;
    private Map<String, FeatureProvider> providers = new ConcurrentHashMap<>();
    private static String DEFAULT_PROVIDER_KEY = "very-secret-string-which-you-shouldnt-use";

    private OpenFeatureAPI() {
        this.apiHooks = new ArrayList<>();
    }

    private static class SingletonHolder {
        private static final OpenFeatureAPI INSTANCE = new OpenFeatureAPI();
    }

    /**
     * Provisions the {@link OpenFeatureAPI} singleton (if needed) and returns it.
     * @return The singleton instance.
     */
    public static OpenFeatureAPI getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Metadata getProviderMetadata() {
        return providers.get(DEFAULT_PROVIDER_KEY).getMetadata();
    }

    public Metadata getProviderMetadata(String clientName) {
        return providers.get(clientName).getMetadata();
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
     * {@inheritDoc}
     */
    public void setProvider(FeatureProvider provider) {
        setProvider(DEFAULT_PROVIDER_KEY, provider);
    }

    public void setProvider(String clientName, FeatureProvider provider) {
        try (AutoCloseableLock __ = providerLock.writeLockAutoCloseable()) {
            this.providers.put(clientName, provider);
        }
    }

    /**
     * {@inheritDoc}
     */
    public FeatureProvider getProvider() {
        return this.providers.get(DEFAULT_PROVIDER_KEY);
    }

    public FeatureProvider getProviderForClientOrDefault(String name) {
        try (AutoCloseableLock __ = providerLock.writeLockAutoCloseable()) {
            FeatureProvider val = this.providers.get(name);
            if (val != null) {
                return val;
            }
            return this.providers.get(DEFAULT_PROVIDER_KEY);
        }
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
