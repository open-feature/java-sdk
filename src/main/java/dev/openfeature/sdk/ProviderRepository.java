package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.exceptions.OpenFeatureError;
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
class ProviderRepository {

    private final Map<String, StatefulFeatureProvider> providers = new ConcurrentHashMap<>();
    private final AtomicReference<StatefulFeatureProvider> defaultProvider = new AtomicReference<>(
            new StatefulFeatureProvider(new NoOpProvider())
    );
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Return the default provider.
     */
    public FeatureProvider getProvider() {
        return defaultProvider.get().getDelegate();
    }

    /**
     * Fetch a provider for a domain. If not found, return the default.
     *
     * @param domain The domain to look for.
     * @return A named {@link FeatureProvider}
     */
    public FeatureProvider getProvider(String domain) {
        if (domain == null) return defaultProvider.get().getDelegate();
        StatefulFeatureProvider fromMap = this.providers.get(domain);
        if (fromMap == null) {
            return this.defaultProvider.get().getDelegate();
        } else {
            return fromMap.getDelegate();
        }
    }

    public ProviderState getProviderState() {
        return defaultProvider.get().getState();
    }

    public ProviderState getProviderState(FeatureProvider featureProvider) {
        if (featureProvider instanceof StatefulFeatureProvider) {
            return ((StatefulFeatureProvider) featureProvider).getState();
        }

        StatefulFeatureProvider defaultProvider = this.defaultProvider.get();
        if (defaultProvider.equals(featureProvider)) {
            return defaultProvider.getState();
        }

        for (StatefulFeatureProvider wrapper : providers.values()) {
            if (wrapper.equals(featureProvider)) {
                return wrapper.getState();
            }
        }
        return null;
    }

    public ProviderState getProviderState(String domain) {
        return Optional.ofNullable(domain).map(this.providers::get).orElse(this.defaultProvider.get()).getState();
    }

    public List<String> getDomainsForProvider(FeatureProvider provider) {
        return providers.entrySet().stream()
                .filter(entry -> entry.getValue().equals(provider))
                .map(entry -> entry.getKey()).collect(Collectors.toList());
    }

    public Set<String> getAllBoundDomains() {
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
    public void setProvider(String domain,
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

    private void prepareAndInitializeProvider(String domain,
                                              FeatureProvider newProvider,
                                              Consumer<FeatureProvider> afterSet,
                                              Consumer<FeatureProvider> afterInit,
                                              Consumer<FeatureProvider> afterShutdown,
                                              BiConsumer<FeatureProvider, OpenFeatureError> afterError,
                                              boolean waitForInit) {

        StatefulFeatureProvider newProviderWrapper = new StatefulFeatureProvider(newProvider);

        if (!isProviderRegistered(newProvider)) {
            // only run afterSet if new provider is not already attached
            afterSet.accept(newProvider);
        }

        // provider is set immediately, on this thread
        StatefulFeatureProvider oldProvider = domain != null
                ? this.providers.put(domain, newProviderWrapper)
                : this.defaultProvider.getAndSet(newProviderWrapper);

        if (waitForInit) {
            initializeProvider(newProviderWrapper, afterInit, afterShutdown, afterError, oldProvider);
        } else {
            taskExecutor.submit(() -> {
                // initialization happens in a different thread if we're not waiting it
                initializeProvider(newProviderWrapper, afterInit, afterShutdown, afterError, oldProvider);
            });
        }
    }

    private void initializeProvider(StatefulFeatureProvider newProvider,
                                    Consumer<FeatureProvider> afterInit,
                                    Consumer<FeatureProvider> afterShutdown,
                                    BiConsumer<FeatureProvider, OpenFeatureError> afterError,
                                    StatefulFeatureProvider oldProvider) {
        try {
            if (ProviderState.NOT_READY.equals(newProvider.getState())) {
                newProvider.initialize(OpenFeatureAPI.getInstance().getEvaluationContext());
                afterInit.accept(newProvider.getDelegate());
            }
            shutDownOld(oldProvider, afterShutdown);
        } catch (OpenFeatureError e) {
            log.error(
                    "Exception when initializing feature provider {}",
                    newProvider.getDelegate().getClass().getName(),
                    e
            );
            afterError.accept(newProvider.getDelegate(), e);
        } catch (Exception e) {
            log.error(
                    "Exception when initializing feature provider {}",
                    newProvider.getDelegate().getClass().getName(),
                    e
            );
            afterError.accept(newProvider.getDelegate(), new GeneralError(e));
        }
    }

    private void shutDownOld(StatefulFeatureProvider oldProvider, Consumer<FeatureProvider> afterShutdown) {
        if (oldProvider != null && !isProviderRegistered(oldProvider)) {
            shutdownProvider(oldProvider);
            afterShutdown.accept(oldProvider.getDelegate());
        }
    }

    /**
     * Helper to check if provider is already known (registered).
     *
     * @param provider provider to check for registration
     * @return boolean true if already registered, false otherwise
     */
    private boolean isProviderRegistered(FeatureProvider provider) {
        return provider != null
                && (this.providers.containsValue(provider) || this.defaultProvider.get().equals(provider));
    }

    private void shutdownProvider(StatefulFeatureProvider provider) {
        if (provider == null) {
            return;
        }
        shutdownProvider(provider.getDelegate());
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
