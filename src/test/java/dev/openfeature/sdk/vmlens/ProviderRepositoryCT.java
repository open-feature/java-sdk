package dev.openfeature.sdk.vmlens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vmlens.api.AllInterleavings;
import com.vmlens.api.Runner;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.OpenFeatureAPITestUtil;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Concurrency tests for ProviderRepository shutdown behavior using VMLens.
 *
 * <p>These tests verify that concurrent shutdown operations are safe and produce
 * consistent results regardless of thread interleaving. Tests operate through
 * the public OpenFeatureAPI since ProviderRepository is package-private.
 */
class ProviderRepositoryCT {

    private FeatureProvider createMockedProvider(String name, AtomicInteger shutdownCounter) throws Exception {
        FeatureProvider provider = mock(FeatureProvider.class);
        when(provider.getMetadata()).thenReturn(() -> name);
        doAnswer(invocation -> {
                    shutdownCounter.incrementAndGet();
                    return null;
                })
                .when(provider)
                .shutdown();
        doAnswer(invocation -> null).when(provider).initialize(any());
        return provider;
    }

    @Test
    void concurrentShutdown_providerShutdownCalledExactlyOnce() throws Exception {
        try (AllInterleavings allInterleavings =
                new AllInterleavings("Concurrent API shutdown - provider called once")) {
            while (allInterleavings.hasNext()) {
                // Fresh state for each interleaving
                AtomicInteger shutdownCount = new AtomicInteger(0);
                FeatureProvider provider = createMockedProvider("test-provider", shutdownCount);
                OpenFeatureAPI api = OpenFeatureAPITestUtil.createAPI();

                // Set provider and wait for initialization to complete
                api.setProviderAndWait(provider);

                // Run concurrent shutdowns through the public API
                Runner.runParallel(api::shutdown, api::shutdown, api::shutdown);

                // INVARIANT: Provider shutdown must be called exactly once
                assertThat(shutdownCount.get())
                        .as("Provider.shutdown() should be called exactly once regardless of thread interleaving")
                        .isEqualTo(1);
            }
        }
    }

    @Test
    void setProviderDuringShutdown_eitherSucceedsOrThrows() throws Exception {
        try (AllInterleavings allInterleavings = new AllInterleavings("setProvider racing with shutdown")) {
            while (allInterleavings.hasNext()) {
                // Fresh state for each interleaving
                AtomicInteger provider1ShutdownCount = new AtomicInteger(0);
                AtomicInteger provider2ShutdownCount = new AtomicInteger(0);
                FeatureProvider provider1 = createMockedProvider("provider-1", provider1ShutdownCount);
                FeatureProvider provider2 = createMockedProvider("provider-2", provider2ShutdownCount);
                OpenFeatureAPI api = OpenFeatureAPITestUtil.createAPI();

                // Set initial provider
                api.setProviderAndWait(provider1);

                // Track outcomes
                AtomicInteger setProviderSucceeded = new AtomicInteger(0);
                AtomicInteger setProviderFailed = new AtomicInteger(0);

                Runner.runParallel(api::shutdown, () -> {
                    try {
                        api.setProvider(provider2);
                        setProviderSucceeded.incrementAndGet();
                    } catch (IllegalStateException e) {
                        if (e.getMessage().contains("shutting down")) {
                            setProviderFailed.incrementAndGet();
                        } else {
                            throw e;
                        }
                    }
                });

                // INVARIANT: setProvider must have exactly one outcome
                int totalOutcomes = setProviderSucceeded.get() + setProviderFailed.get();
                assertThat(totalOutcomes)
                        .as("setProvider must have exactly one outcome (success or failure)")
                        .isEqualTo(1);

                // INVARIANT: Original provider should always be shut down
                assertThat(provider1ShutdownCount.get())
                        .as("Original provider should be shut down exactly once")
                        .isEqualTo(1);
            }
        }
    }

    @Test
    void concurrentShutdown_multipleProvidersShutdownExactlyOnce() throws Exception {
        try (AllInterleavings allInterleavings = new AllInterleavings("Concurrent shutdown - multiple providers")) {
            while (allInterleavings.hasNext()) {
                AtomicInteger provider1ShutdownCount = new AtomicInteger(0);
                AtomicInteger provider2ShutdownCount = new AtomicInteger(0);

                FeatureProvider provider1 = createMockedProvider("provider-1", provider1ShutdownCount);
                FeatureProvider provider2 = createMockedProvider("provider-2", provider2ShutdownCount);

                OpenFeatureAPI api = OpenFeatureAPITestUtil.createAPI();

                // Register providers to named domains
                api.setProviderAndWait("domain-1", provider1);
                api.setProviderAndWait("domain-2", provider2);

                // Run concurrent shutdowns
                Runner.runParallel(api::shutdown, api::shutdown);

                // INVARIANT: Each provider shut down exactly once
                assertThat(provider1ShutdownCount.get())
                        .as("Provider 1 shutdown count")
                        .isEqualTo(1);
                assertThat(provider2ShutdownCount.get())
                        .as("Provider 2 shutdown count")
                        .isEqualTo(1);
            }
        }
    }
}
