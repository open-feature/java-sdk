package dev.openfeature.sdk.multiprovider;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.ProviderEvent;
import dev.openfeature.sdk.ProviderEventDetails;
import dev.openfeature.sdk.ProviderState;
import dev.openfeature.sdk.TrackingEventDetails;
import dev.openfeature.sdk.Value;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MultiProviderEventsAndTrackingTest {

    @Test
    void shouldAggregateChildProviderStateAndForwardConfigurationEvents() throws Exception {
        TrackingProvider provider1 = new TrackingProvider("provider1");
        TrackingProvider provider2 = new TrackingProvider("provider2");
        MultiProvider multiProvider = new MultiProvider(List.of(provider1, provider2));

        OpenFeatureAPI api = new TestOpenFeatureAPI();
        api.shutdown();
        try {
            api.setProviderAndWait("multiProviderEvents", multiProvider);
            Client client = api.getClient("multiProviderEvents");

            await().atMost(Duration.ofSeconds(2)).until(() -> client.getProviderState() == ProviderState.READY);

            AtomicInteger configurationChangedCount = new AtomicInteger();
            client.onProviderConfigurationChanged(details -> configurationChangedCount.incrementAndGet());

            provider1.emitProviderConfigurationChanged(ProviderEventDetails.builder().message("changed").build()).await();
            await().atMost(Duration.ofSeconds(2)).until(() -> configurationChangedCount.get() == 1);

            provider1.emitProviderStale(ProviderEventDetails.builder().message("stale").build()).await();
            await().atMost(Duration.ofSeconds(2)).until(() -> client.getProviderState() == ProviderState.STALE);

            provider2.emitProviderError(
                            ProviderEventDetails.builder().errorCode(dev.openfeature.sdk.ErrorCode.GENERAL).build())
                    .await();
            await().atMost(Duration.ofSeconds(2)).until(() -> client.getProviderState() == ProviderState.ERROR);

            provider2.emitProviderReady(ProviderEventDetails.builder().build()).await();
            await().atMost(Duration.ofSeconds(2)).until(() -> client.getProviderState() == ProviderState.STALE);

            provider1.emitProviderReady(ProviderEventDetails.builder().build()).await();
            await().atMost(Duration.ofSeconds(2)).until(() -> client.getProviderState() == ProviderState.READY);

            provider1.emitProviderError(
                            ProviderEventDetails.builder()
                                    .errorCode(dev.openfeature.sdk.ErrorCode.PROVIDER_FATAL)
                                    .build())
                    .await();
            await().atMost(Duration.ofSeconds(2)).until(() -> client.getProviderState() == ProviderState.FATAL);
        } finally {
            api.shutdown();
        }
    }

    @Test
    void shouldPreserveChildStateEmittedDuringInitialize() throws Exception {
        TrackingProvider provider1 = new InitializingStateProvider("provider1", ProviderState.STALE);
        TrackingProvider provider2 = new TrackingProvider("provider2");
        MultiProvider multiProvider = new MultiProvider(List.of(provider1, provider2));
        List<ProviderEvent> emittedEvents = new CopyOnWriteArrayList<>();
        multiProvider.addEventObserver((event, details) -> emittedEvents.add(event));

        multiProvider.initialize(null);

        await().atMost(Duration.ofSeconds(2)).until(() -> emittedEvents.contains(ProviderEvent.PROVIDER_STALE));
        assertEquals(List.of(ProviderEvent.PROVIDER_STALE), emittedEvents);
    }

    @Test
    void shouldForwardTrackToReadyProvidersAndSkipFatalProviders() throws Exception {
        TrackingProvider provider1 = new TrackingProvider("provider1");
        TrackingProvider provider2 = new TrackingProvider("provider2");
        provider2.throwOnTrack = true;

        MultiProvider multiProvider = new MultiProvider(List.of(provider1, provider2));
        multiProvider.initialize(null);

        multiProvider.track("event1", null, null);
        assertEquals(1, provider1.trackCount.get());
        assertEquals(1, provider2.trackCount.get());

        provider1.emitProviderError(
                        ProviderEventDetails.builder()
                                .errorCode(dev.openfeature.sdk.ErrorCode.PROVIDER_FATAL)
                                .build())
                .await();

        multiProvider.track("event2", null, null);
        assertEquals(1, provider1.trackCount.get());
        assertEquals(2, provider2.trackCount.get());
    }

    static class TrackingProvider extends EventProvider {
        private final String name;
        private final AtomicInteger trackCount = new AtomicInteger();
        private boolean throwOnTrack;

        TrackingProvider(String name) {
            this.name = name;
        }

        @Override
        public Metadata getMetadata() {
            return () -> name;
        }

        @Override
        public void track(String eventName, EvaluationContext context, TrackingEventDetails details) {
            trackCount.incrementAndGet();
            if (throwOnTrack) {
                throw new RuntimeException("track failure");
            }
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Boolean>builder().value(Boolean.TRUE).build();
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<String>builder().value("value").build();
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Integer>builder().value(1).build();
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Double>builder().value(1d).build();
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            return ProviderEvaluation.<Value>builder().value(new Value("value")).build();
        }
    }

    static class InitializingStateProvider extends TrackingProvider {
        private final ProviderState initializeState;

        InitializingStateProvider(String name, ProviderState initializeState) {
            super(name);
            this.initializeState = initializeState;
        }

        @Override
        public void initialize(EvaluationContext evaluationContext) throws Exception {
            if (ProviderState.STALE.equals(initializeState)) {
                emitProviderStale(ProviderEventDetails.builder().message("stale during init").build()).await();
            } else if (ProviderState.FATAL.equals(initializeState)) {
                emitProviderError(ProviderEventDetails.builder()
                                .errorCode(dev.openfeature.sdk.ErrorCode.PROVIDER_FATAL)
                                .message("fatal during init")
                                .build())
                        .await();
            } else if (ProviderState.ERROR.equals(initializeState)) {
                emitProviderError(ProviderEventDetails.builder()
                                .errorCode(dev.openfeature.sdk.ErrorCode.GENERAL)
                                .message("error during init")
                                .build())
                        .await();
            }
        }
    }

    static class TestOpenFeatureAPI extends OpenFeatureAPI {
        TestOpenFeatureAPI() {
            super();
        }
    }
}
