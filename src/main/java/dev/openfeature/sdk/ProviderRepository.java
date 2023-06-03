package dev.openfeature.sdk;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
class ProviderRepository {

    private final Map<String, FeatureProvider> providers = new ConcurrentHashMap<>();
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private final Map<String, FeatureProvider> initializingNamedProviders = new ConcurrentHashMap<>();
    private final AtomicReference<FeatureProvider> defaultProvider = new AtomicReference<>(new NoOpProvider());
    private FeatureProvider initializingDefaultProvider;

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
        return Optional.ofNullable(name).map(this.providers::get).orElse(this.defaultProvider.get());
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
        if (clientName == null) {
            throw new IllegalArgumentException("clientName cannot be null");
        }
        initializeProvider(clientName, provider);
    }

    private void initializeProvider(FeatureProvider provider) {
        initializingDefaultProvider = provider;
        initializeProvider(provider, this::updateDefaultProviderAfterInitialization);
    }

    private void initializeProvider(String clientName, FeatureProvider provider) {
        initializingNamedProviders.put(clientName, provider);
        initializeProvider(provider, newProvider -> updateProviderAfterInit(clientName, newProvider));
    }

    private void initializeProvider(FeatureProvider provider, Consumer<FeatureProvider> afterInitialization) {
        taskExecutor.submit(() -> {
            try {
                if (isProviderNotBoundMultipleTimes(provider)) {
                    provider.initialize();
                }
                afterInitialization.accept(provider);
            } catch (Exception e) {
                log.error("Exception when initializing feature provider {}", provider.getClass().getName(), e);
            }
        });
    }

    private void updateProviderAfterInit(String clientName, FeatureProvider newProvider) {
        Optional
                .ofNullable(initializingNamedProviders.get(clientName))
                .filter(initializingProvider -> initializingProvider.equals(newProvider))
                .ifPresent(provider -> updateNamedProviderAfterInitialization(clientName, provider));
    }

    private void updateDefaultProviderAfterInitialization(FeatureProvider initializedProvider) {
        Optional
                .ofNullable(this.initializingDefaultProvider)
                .filter(initializingProvider -> initializingProvider.equals(initializedProvider))
                .ifPresent(this::replaceDefaultProvider);
    }

    private void replaceDefaultProvider(FeatureProvider provider) {
        FeatureProvider oldProvider = this.defaultProvider.getAndSet(provider);
        if (isOldProviderNotBoundByName(oldProvider)) {
            shutdownProvider(oldProvider);
        }
    }

    private boolean isOldProviderNotBoundByName(FeatureProvider oldProvider) {
        return !this.providers.containsValue(oldProvider);
    }

    private void updateNamedProviderAfterInitialization(String clientName, FeatureProvider initializedProvider) {
        Optional
                .ofNullable(this.initializingNamedProviders.get(clientName))
                .filter(initializingProvider -> initializingProvider.equals(initializedProvider))
                .ifPresent(provider -> replaceNamedProviderAndShutdownOldOne(clientName, provider));
    }

    private void replaceNamedProviderAndShutdownOldOne(String clientName, FeatureProvider provider) {
        FeatureProvider oldProvider = this.providers.put(clientName, provider);
        this.initializingNamedProviders.remove(clientName, provider);
        if (isProviderNotBoundMultipleTimes(oldProvider)) {
            shutdownProvider(oldProvider);
        }
    }

    private boolean isProviderNotBoundMultipleTimes(FeatureProvider oldProvider) {
        return !(this.providers.containsValue(oldProvider) || this.defaultProvider.get().equals(oldProvider));
    }

    private void shutdownProvider(FeatureProvider provider) {
        taskExecutor.submit(() -> {
            try {
                provider.shutdown();
            } catch (Exception e) {
                log.error("Exception when shutting down feature provider {}", provider.getClass().getName(), e);
            }
        });
    }

    /**
     * Shutdowns this repository which includes shutting down all FeatureProviders that are registered,
     * including the default feature provider.
     */
    public void shutdown() {
        Stream
                .concat(Stream.of(this.defaultProvider.get()), this.providers.values().stream())
                .distinct()
                .forEach(this::shutdownProvider);
        setProvider(new NoOpProvider());
        this.providers.clear();
        taskExecutor.shutdown();
    }
}
