package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * Util class for storing and running handlers.
 */
@Slf4j
class EventSupport {

    private static final String defaultClientUuid = UUID.randomUUID().toString();
    private static final ExecutorService taskExecutor = Executors.newCachedThreadPool();
    private final Map<String, HandlerStore> handlerStores = new ConcurrentHashMap<>();
    private final HandlerStore globalHanderStore = new HandlerStore();

    public void runClientHandlers(@Nullable String clientName, ProviderEvent event, EventDetails eventDetails) {
        clientName = Optional.ofNullable(clientName)
                .orElse(defaultClientUuid);

        Optional.ofNullable(handlerStores.get(clientName))
                .filter(store -> Optional.of(store).isPresent())
                .map(store -> store.handlerMap.get(event))
                .ifPresent(handlers -> handlers
                        .forEach(handler -> runHandler(handler, eventDetails)));
    }

    public void runGlobalHandlers(ProviderEvent event, EventDetails eventDetails) {
        globalHanderStore.handlerMap.get(event)
                .forEach(handler -> {
                    runHandler(handler, eventDetails);
                });
    }

    public void addClientHandler(@Nullable String clientName, ProviderEvent event, Consumer<EventDetails> handler) {
        final String name = Optional.ofNullable(clientName)
                .orElse(defaultClientUuid);

        HandlerStore store = Optional.ofNullable(this.handlerStores.get(name))
                .orElseGet(() -> {
                    HandlerStore newStore = new HandlerStore();
                    this.handlerStores.put(name, newStore);
                    return newStore;
                });
        if (this.handlerStores.get(name) == null) {
            // TODO: test for not adding tons of handler stores
            // Note we create spaces for client handlers lazily, not with every named client.
            this.handlerStores.put(name, new HandlerStore());
        }
        store.addHandler(event, handler);
    }

    public void removeClientHandler(String clientName, ProviderEvent event, Consumer<EventDetails> handler) {
        clientName = Optional.ofNullable(clientName)
                .orElse(defaultClientUuid);
        this.handlerStores.get(clientName).removeHandler(event, handler);
    }

    public void addGlobalHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        this.globalHanderStore.addHandler(event, handler);
    }

    public void removeGlobalHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        this.globalHanderStore.removeHandler(event, handler);
    }

    public Set<String> getAllClientNames() {
        return this.handlerStores.keySet();
    }

    private void runHandler(Consumer<EventDetails> handler, EventDetails eventDetails) {
        taskExecutor.submit(() -> {
            try {
                handler.accept(eventDetails);
            } catch (Exception e) {
                log.error("Exception in event handler {}", handler, e);
            }
        });
    }

    class HandlerStore {

        private final Map<ProviderEvent, List<Consumer<EventDetails>>> handlerMap;

        {
            handlerMap = new ConcurrentHashMap<ProviderEvent, List<Consumer<EventDetails>>>();
            handlerMap.put(ProviderEvent.PROVIDER_READY, new ArrayList<>());
            handlerMap.put(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, new ArrayList<>());
            handlerMap.put(ProviderEvent.PROVIDER_ERROR, new ArrayList<>());
            handlerMap.put(ProviderEvent.PROVIDER_STALE, new ArrayList<>());
        }

        void addHandler(ProviderEvent event, Consumer<EventDetails> handler) {
            handlerMap.get(event).add(handler);
        }

        void removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
            handlerMap.get(event).remove(handler);
        }
    }
}
