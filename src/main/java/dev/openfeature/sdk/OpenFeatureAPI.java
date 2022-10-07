package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import dev.openfeature.sdk.internal.AutoCloseableLock;
import dev.openfeature.sdk.internal.AutoCloseableReentrantReadWriteLock;
import lombok.Getter;
import lombok.Setter;

/**
 * A global singleton which holds base configuration for the OpenFeature library.
 * Configuration here will be shared across all {@link Client}s.
 */
public class OpenFeatureAPI {
    // package-private multi-read/single-write lock
    static AutoCloseableReentrantReadWriteLock rwLock = new AutoCloseableReentrantReadWriteLock();
    @Getter
    private FeatureProvider provider;
    @Getter
    @Setter
    private EvaluationContext evaluationContext;
    @Getter
    private List<Hook> apiHooks;

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
        return provider.getMetadata();
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
    public void setProvider(FeatureProvider provider) {
        try (AutoCloseableLock __ = rwLock.writeLockAutoCloseable()) {
            this.provider = provider;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addHooks(Hook... hooks) {
        try (AutoCloseableLock __ = rwLock.writeLockAutoCloseable()) {
            this.apiHooks.addAll(Arrays.asList(hooks));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clearHooks() {
        try (AutoCloseableLock __ = rwLock.writeLockAutoCloseable()) {
            this.apiHooks.clear();
        }
    }
}
