package dev.openfeature.sdk;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ProviderRepository {

    private final Map<String, FeatureProvider> providers = new ConcurrentHashMap<>();
    private final AtomicReference<FeatureProvider> defaultProvider = new AtomicReference<>(new NoOpProvider());
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool();

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

    public List<String> getClientNamesForProvider(FeatureProvider provider) {
        return providers.entrySet().stream()
                .filter(entry -> entry.getValue().equals(provider))
                .map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    public Set<String> getAllBoundClientNames() {
        return providers.keySet();
    }

    public boolean isDefaultProvider(FeatureProvider provider) {
        return this.getProvider().equals(provider);
    }

    /**
     * Set the default provider.
     */
    public void setProvider(FeatureProvider provider,
            Consumer<FeatureProvider> afterSet,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, String> afterError) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        initializeProvider(null, provider, afterSet, afterInit, afterShutdown, afterError);
    }

    /**
     * Add a provider for a named client.
     *
     * @param clientName The name of the client.
     * @param provider   The provider to set.
     */
    public void setProvider(String clientName,
            FeatureProvider provider,
            Consumer<FeatureProvider> afterSet,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, String> afterError) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        if (clientName == null) {
            throw new IllegalArgumentException("clientName cannot be null");
        }
        initializeProvider(clientName, provider, afterSet, afterInit, afterShutdown, afterError);
    }

    private void initializeProvider(@Nullable String clientName,
            FeatureProvider newProvider,
            Consumer<FeatureProvider> afterSet,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, String> afterError) {
        // provider is set immediately, on this thread
        FeatureProvider oldProvider = clientName != null
                ? this.providers.put(clientName, newProvider)
                : this.defaultProvider.getAndSet(newProvider);
        afterSet.accept(newProvider);
        taskExecutor.submit(() -> {
            // initialization happens in a different thread
            try {
                if (ProviderState.NOT_READY.equals(newProvider.getState())) {
                    newProvider.initialize(OpenFeatureAPI.getInstance().getEvaluationContext());
                }
                emitReadyAndShutdownOld(clientName, newProvider, oldProvider, afterInit, afterShutdown);
            } catch (Exception e) {
                log.error("Exception when initializing feature provider {}", newProvider.getClass().getName(), e);
                afterError.accept(newProvider, e.getMessage());
            }
        });
    }

    private void emitReadyAndShutdownOld(@Nullable String clientName, FeatureProvider newProvider,
            FeatureProvider oldProvider, Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown) {
        afterInit.accept(newProvider);
        if (!isProviderRegistered(oldProvider)) {
            shutdownProvider(oldProvider);
            afterShutdown.accept(oldProvider);
        }
    }

    private boolean isProviderRegistered(FeatureProvider oldProvider) {
        return this.providers.containsValue(oldProvider) || this.defaultProvider.get().equals(oldProvider);
    }

    private void shutdownProvider(FeatureProvider provider) {
        taskExecutor.submit(() -> {
            try {
                // detachProviderEvents(provider);
                provider.shutdown();
            } catch (Exception e) {
                log.error("Exception when shutting down feature provider {}", provider.getClass().getName(), e);
            }
        });
    }

    /**
     * Shuts down this repository which includes shutting down all FeatureProviders
     * that are registered,
     * including the default feature provider.
     */
    public void shutdown() {
        Stream
                .concat(Stream.of(this.defaultProvider.get()), this.providers.values().stream())
                .distinct()
                .forEach(this::shutdownProvider);
        setProvider(new NoOpProvider(),
                (FeatureProvider fp) -> {
                },
                (FeatureProvider fp) -> {
                },
                (FeatureProvider fp) -> {
                },
                (FeatureProvider fp,
                        String message) -> {
                });
        this.providers.clear();
        taskExecutor.shutdown();
    }
}
