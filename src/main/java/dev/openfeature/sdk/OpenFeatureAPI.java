package dev.openfeature.sdk;

import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A global singleton which holds base configuration for the OpenFeature library.
 * Configuration here will be shared across all {@link Client}s.
 */
@Slf4j
public class OpenFeatureAPI {
    // package-private multi-read/single-write lock
    static AutoCloseableReentrantReadWriteLock hooksLock = new AutoCloseableReentrantReadWriteLock();
    static AutoCloseableReentrantReadWriteLock contextLock = new AutoCloseableReentrantReadWriteLock();

    private final ProviderRepository providerRepository = new ProviderRepository();
    private final List<Hook> apiHooks;

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
        return getProvider().getMetadata();
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
        providerRepository.setProvider(provider);
    }

    /**
     * Add a provider for a named client.
     *
     * @param clientName The name of the client.
     * @param provider   The provider to set.
     */
    public void setProvider(String clientName, FeatureProvider provider) {
        providerRepository.setProvider(clientName, provider);
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
