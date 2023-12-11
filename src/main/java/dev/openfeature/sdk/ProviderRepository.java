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
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

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
            BiConsumer<FeatureProvider, String> afterError,
            boolean waitForInit) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        prepareAndInitializeProvider(null, provider, afterSet, afterInit, afterShutdown, afterError, waitForInit);
    }

    /**
     * Add a provider for a named client.
     *
     * @param clientName  The name of the client.
     * @param provider    The provider to set.
     * @param waitForInit When true, wait for initialization to finish, then returns.
     *                    Otherwise, initialization happens in the background.
     */
    public void setProvider(String clientName,
             FeatureProvider provider,
             Consumer<FeatureProvider> afterSet,
             Consumer<FeatureProvider> afterInit,
             Consumer<FeatureProvider> afterShutdown,
             BiConsumer<FeatureProvider, String> afterError,
             boolean waitForInit) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        if (clientName == null) {
            throw new IllegalArgumentException("clientName cannot be null");
        }
        prepareAndInitializeProvider(clientName, provider, afterSet, afterInit, afterShutdown, afterError, waitForInit);
    }

    private void prepareAndInitializeProvider(@Nullable String clientName,
              FeatureProvider newProvider,
              Consumer<FeatureProvider> afterSet,
              Consumer<FeatureProvider> afterInit,
              Consumer<FeatureProvider> afterShutdown,
              BiConsumer<FeatureProvider, String> afterError,
              boolean waitForInit) {

        // provider is set immediately, on this thread
        FeatureProvider oldProvider = clientName != null
                ? this.providers.put(clientName, newProvider)
                : this.defaultProvider.getAndSet(newProvider);

        if (!isProviderRegistered(newProvider)) {
            // only run afterSet if new provider is not already attached
            afterSet.accept(newProvider);
        }

        if (waitForInit) {
            initializeProvider(newProvider, afterInit, afterShutdown, afterError, oldProvider);
        } else {
            taskExecutor.submit(() -> {
                // initialization happens in a different thread if we're not waiting it
                initializeProvider(newProvider, afterInit, afterShutdown, afterError, oldProvider);
            });
        }
    }

    private void initializeProvider(FeatureProvider newProvider,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, String> afterError,
            FeatureProvider oldProvider) {
        try {
            if (ProviderState.NOT_READY.equals(newProvider.getState())) {
                newProvider.initialize(OpenFeatureAPI.getInstance().getEvaluationContext());
                afterInit.accept(newProvider);
            }
            shutDownOld(oldProvider, afterShutdown);
        } catch (Exception e) {
            log.error("Exception when initializing feature provider {}", newProvider.getClass().getName(), e);
            afterError.accept(newProvider, e.getMessage());
        }
    }

    private void shutDownOld(FeatureProvider oldProvider, Consumer<FeatureProvider> afterShutdown) {
        if (!isProviderRegistered(oldProvider)) {
            shutdownProvider(oldProvider);
            afterShutdown.accept(oldProvider);
        }
    }

    /**
     * Helper to check if provider is already known (registered).
     * @param provider provider to check for registration
     * @return boolean true if already registered, false otherwise
     */
    private boolean isProviderRegistered(FeatureProvider provider) {
        return provider != null
                && (this.providers.containsValue(provider) || this.defaultProvider.get().equals(provider));
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
        this.providers.clear();
        taskExecutor.shutdown();
    }
}
