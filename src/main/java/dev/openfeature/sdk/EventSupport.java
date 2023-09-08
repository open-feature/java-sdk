package dev.openfeature.sdk;

import edu.umd.cs.findbugs.annotations.Nullable;
import lombok.extern.slf4j.Slf4j;

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

/**
 * Util class for storing and running handlers.
 */
@Slf4j
class EventSupport {

    // we use a v4 uuid as a "placeholder" for anonymous clients, since
    // ConcurrentHashMap doesn't support nulls
    private static final String defaultClientUuid = UUID.randomUUID().toString();
    private final Map<String, HandlerStore> handlerStores = new ConcurrentHashMap<>();
    private final HandlerStore globalHandlerStore = new HandlerStore();
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Run all the event handlers associated with this client name.
     * If the client name is null, handlers attached to unnamed clients will run.
     * 
     * @param clientName   the client name to run event handlers for, or null
     * @param event        the event type
     * @param eventDetails the event details
     */
    public void runClientHandlers(@Nullable String clientName, ProviderEvent event, EventDetails eventDetails) {
        clientName = Optional.ofNullable(clientName)
                .orElse(defaultClientUuid);

        // run handlers if they exist
        Optional.ofNullable(handlerStores.get(clientName))
                .filter(store -> Optional.of(store).isPresent())
                .map(store -> store.handlerMap.get(event))
                .ifPresent(handlers -> handlers
                        .forEach(handler -> runHandler(handler, eventDetails)));
    }

    /**
     * Run all the API (global) event handlers.
     * 
     * @param event        the event type
     * @param eventDetails the event details
     */
    public void runGlobalHandlers(ProviderEvent event, EventDetails eventDetails) {
        globalHandlerStore.handlerMap.get(event)
                .forEach(handler -> {
                    runHandler(handler, eventDetails);
                });
    }

    /**
     * Add a handler for the specified client name, or all unnamed clients.
     * 
     * @param clientName the client name to add handlers for, or else the unnamed
     *                   client
     * @param event      the event type
     * @param handler    the handler function to run
     */
    public void addClientHandler(@Nullable String clientName, ProviderEvent event, Consumer<EventDetails> handler) {
        final String name = Optional.ofNullable(clientName)
                .orElse(defaultClientUuid);

        // lazily create and cache a HandlerStore if it doesn't exist
        HandlerStore store = Optional.ofNullable(this.handlerStores.get(name))
                .orElseGet(() -> {
                    HandlerStore newStore = new HandlerStore();
                    this.handlerStores.put(name, newStore);
                    return newStore;
                });
        store.addHandler(event, handler);
    }

    /**
     * Remove a client event handler for the specified event type.
     * 
     * @param clientName the name of the client handler to remove, or null to remove
     *                   from unnamed clients
     * @param event      the event type
     * @param handler    the handler ref to be removed
     */
    public void removeClientHandler(String clientName, ProviderEvent event, Consumer<EventDetails> handler) {
        clientName = Optional.ofNullable(clientName)
                .orElse(defaultClientUuid);
        this.handlerStores.get(clientName).removeHandler(event, handler);
    }

    /**
     * Add a global event handler of the specified event type.
     * 
     * @param event   the event type
     * @param handler the handler to be added
     */
    public void addGlobalHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        this.globalHandlerStore.addHandler(event, handler);
    }

    /**
     * Remove a global event handler for the specified event type.
     * 
     * @param event   the event type
     * @param handler the handler ref to be removed
     */
    public void removeGlobalHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        this.globalHandlerStore.removeHandler(event, handler);
    }

    /**
     * Get all client names for which we have event handlers registered.
     * 
     * @return set of client names
     */
    public Set<String> getAllClientNames() {
        return this.handlerStores.keySet();
    }

    /**
     * Run the passed handler on the taskExecutor.
     * 
     * @param handler      the handler to run
     * @param eventDetails the event details
     */
    public void runHandler(Consumer<EventDetails> handler, EventDetails eventDetails) {
        taskExecutor.submit(() -> {
            try {
                handler.accept(eventDetails);
            } catch (Exception e) {
                log.error("Exception in event handler {}", handler, e);
            }
        });
    }

    /**
     * Stop the event handler task executor.
     */
    public void shutdown() {
        try {
            taskExecutor.shutdown();
        } catch (Exception e) {
            log.warn("Exception while attempting to shutdown task executor", e);
        }
    }

    // Handler store maintains a set of handlers for each event type.
    // Each client in the SDK gets it's own handler store, which is lazily
    // instantiated when a handler is added to that client.
    static class HandlerStore {

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
