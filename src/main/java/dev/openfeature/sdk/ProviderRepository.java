package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import dev.openfeature.sdk.internal.ConfigurableThreadFactory;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ProviderRepository {

    private final Map<String, FeatureProviderStateManager> stateManagers = new ConcurrentHashMap<>();
    private final AtomicReference<FeatureProviderStateManager> defaultStateManger =
            new AtomicReference<>(new FeatureProviderStateManager(new NoOpProvider()));
    private final ExecutorService taskExecutor =
            Executors.newCachedThreadPool(new ConfigurableThreadFactory("openfeature-provider-thread", true));
    private final Object registerStateManagerLock = new Object();
    private final OpenFeatureAPI openFeatureAPI;

    public ProviderRepository(OpenFeatureAPI openFeatureAPI) {
        this.openFeatureAPI = openFeatureAPI;
    }

    FeatureProviderStateManager getFeatureProviderStateManager() {
        return defaultStateManger.get();
    }

    FeatureProviderStateManager getFeatureProviderStateManager(String domain) {
        if (domain == null) {
            return defaultStateManger.get();
        }
        FeatureProviderStateManager fromMap = this.stateManagers.get(domain);
        if (fromMap == null) {
            return this.defaultStateManger.get();
        } else {
            return fromMap;
        }
    }

    /**
     * Return the default provider.
     */
    public FeatureProvider getProvider() {
        return defaultStateManger.get().getProvider();
    }

    /**
     * Fetch a provider for a domain. If not found, return the default.
     *
     * @param domain The domain to look for.
     * @return A named {@link FeatureProvider}
     */
    public FeatureProvider getProvider(String domain) {
        return getFeatureProviderStateManager(domain).getProvider();
    }

    public ProviderState getProviderState() {
        return getFeatureProviderStateManager().getState();
    }

    public ProviderState getProviderState(FeatureProvider featureProvider) {
        if (featureProvider instanceof FeatureProviderStateManager) {
            return ((FeatureProviderStateManager) featureProvider).getState();
        }

        FeatureProviderStateManager defaultProvider = this.defaultStateManger.get();
        if (defaultProvider.hasSameProvider(featureProvider)) {
            return defaultProvider.getState();
        }

        for (FeatureProviderStateManager wrapper : stateManagers.values()) {
            if (wrapper.hasSameProvider(featureProvider)) {
                return wrapper.getState();
            }
        }
        return null;
    }

    public ProviderState getProviderState(String domain) {
        return Optional.ofNullable(domain)
                .map(this.stateManagers::get)
                .orElse(this.defaultStateManger.get())
                .getState();
    }

    public List<String> getDomainsForProvider(FeatureProvider provider) {
        return stateManagers.entrySet().stream()
                .filter(entry -> entry.getValue().hasSameProvider(provider))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Set<String> getAllBoundDomains() {
        return stateManagers.keySet();
    }

    public boolean isDefaultProvider(FeatureProvider provider) {
        return this.getProvider().equals(provider);
    }

    /**
     * Set the default provider.
     */
    public void setProvider(
            FeatureProvider provider,
            Consumer<FeatureProvider> afterSet,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, OpenFeatureError> afterError,
            boolean waitForInit) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        prepareAndInitializeProvider(null, provider, afterSet, afterInit, afterShutdown, afterError, waitForInit);
    }

    /**
     * Add a provider for a domain.
     *
     * @param domain      The domain to bind the provider to.
     * @param provider    The provider to set.
     * @param waitForInit When true, wait for initialization to finish, then returns.
     *                    Otherwise, initialization happens in the background.
     */
    public void setProvider(
            String domain,
            FeatureProvider provider,
            Consumer<FeatureProvider> afterSet,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, OpenFeatureError> afterError,
            boolean waitForInit) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        if (domain == null) {
            throw new IllegalArgumentException("domain cannot be null");
        }
        prepareAndInitializeProvider(domain, provider, afterSet, afterInit, afterShutdown, afterError, waitForInit);
    }

    private void prepareAndInitializeProvider(
            String domain,
            FeatureProvider newProvider,
            Consumer<FeatureProvider> afterSet,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, OpenFeatureError> afterError,
            boolean waitForInit) {
        final FeatureProviderStateManager newStateManager;
        final FeatureProviderStateManager oldStateManager;

        synchronized (registerStateManagerLock) {
            FeatureProviderStateManager existing = getExistingStateManagerForProvider(newProvider);
            if (existing == null) {
                newStateManager = new FeatureProviderStateManager(newProvider);
                // only run afterSet if new provider is not already attached
                afterSet.accept(newProvider);
            } else {
                newStateManager = existing;
            }

            // provider is set immediately, on this thread
            oldStateManager = domain != null
                    ? this.stateManagers.put(domain, newStateManager)
                    : this.defaultStateManger.getAndSet(newStateManager);
        }

        if (waitForInit) {
            initializeProvider(newStateManager, afterInit, afterShutdown, afterError, oldStateManager);
        } else {
            taskExecutor.submit(() -> {
                // initialization happens in a different thread if we're not waiting for it
                initializeProvider(newStateManager, afterInit, afterShutdown, afterError, oldStateManager);
            });
        }
    }

    private FeatureProviderStateManager getExistingStateManagerForProvider(FeatureProvider provider) {
        for (FeatureProviderStateManager stateManager : stateManagers.values()) {
            if (stateManager.hasSameProvider(provider)) {
                return stateManager;
            }
        }
        FeatureProviderStateManager defaultFeatureProviderStateManager = defaultStateManger.get();
        if (defaultFeatureProviderStateManager.hasSameProvider(provider)) {
            return defaultFeatureProviderStateManager;
        }
        return null;
    }

    private void initializeProvider(
            FeatureProviderStateManager newManager,
            Consumer<FeatureProvider> afterInit,
            Consumer<FeatureProvider> afterShutdown,
            BiConsumer<FeatureProvider, OpenFeatureError> afterError,
            FeatureProviderStateManager oldManager) {
        try {
            if (ProviderState.NOT_READY.equals(newManager.getState())) {
                newManager.initialize(openFeatureAPI.getEvaluationContext());
                afterInit.accept(newManager.getProvider());
            }
            shutDownOld(oldManager, afterShutdown);
        } catch (OpenFeatureError e) {
            log.error(
                    "Exception when initializing feature provider {}",
                    newManager.getProvider().getClass().getName(),
                    e);
            afterError.accept(newManager.getProvider(), e);
        } catch (Exception e) {
            log.error(
                    "Exception when initializing feature provider {}",
                    newManager.getProvider().getClass().getName(),
                    e);
            afterError.accept(newManager.getProvider(), new GeneralError(e));
        }
    }

    private void shutDownOld(FeatureProviderStateManager oldManager, Consumer<FeatureProvider> afterShutdown) {
        if (oldManager != null && !isStateManagerRegistered(oldManager)) {
            shutdownProvider(oldManager);
            afterShutdown.accept(oldManager.getProvider());
        }
    }

    /**
     * Helper to check if manager is already known (registered).
     *
     * @param manager manager to check for registration
     * @return boolean true if already registered, false otherwise
     */
    private boolean isStateManagerRegistered(FeatureProviderStateManager manager) {
        return manager != null
                && (this.stateManagers.containsValue(manager)
                        || this.defaultStateManger.get().equals(manager));
    }

    private void shutdownProvider(FeatureProviderStateManager manager) {
        if (manager == null) {
            return;
        }
        shutdownProvider(manager.getProvider());
    }

    private void shutdownProvider(FeatureProvider provider) {
        taskExecutor.submit(() -> {
            try {
                provider.shutdown();
            } catch (Exception e) {
                log.error(
                        "Exception when shutting down feature provider {}",
                        provider.getClass().getName(),
                        e);
            }
        });
    }

    /**
     * Shuts down this repository which includes shutting down all FeatureProviders
     * that are registered,
     * including the default feature provider.
     */
    public void shutdown() {
        Stream.concat(Stream.of(this.defaultStateManger.get()), this.stateManagers.values().stream())
                .distinct()
                .forEach(this::shutdownProvider);
        this.stateManagers.clear();
        taskExecutor.shutdown();
    }
}
