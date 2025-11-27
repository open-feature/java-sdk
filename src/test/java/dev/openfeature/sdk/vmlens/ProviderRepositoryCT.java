package dev.openfeature.sdk.vmlens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.vmlens.api.AllInterleavings;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.OpenFeatureAPITestUtil;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Concurrency tests for ProviderRepository shutdown behavior using VMLens.
 *
 * These tests verify that concurrent shutdown operations are safe and produce
 * consistent results regardless of thread interleaving. Tests operate through
 * the public OpenFeatureAPI since ProviderRepository is package-private.
 *
 */
class ProviderRepositoryCT {

    private FeatureProvider createMockedProvider(String name, AtomicInteger shutdownCounter) {
        FeatureProvider provider = mock(FeatureProvider.class);
        when(provider.getMetadata()).thenReturn(() -> name);
        doAnswer(invocation -> {
            shutdownCounter.incrementAndGet();
            return null;
        }).when(provider).shutdown();
        try {
            doAnswer(invocation -> null).when(provider).initialize(any());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return provider;
    }

    /**
     * Test: When multiple threads call shutdown() concurrently, the provider's
     * shutdown() method should be called exactly once.
     *
     * This verifies that the isShuttingDown guard in ProviderRepository correctly
     * prevents multiple threads from executing the shutdown logic.
     */
    @Test
    void concurrentShutdown_providerShutdownCalledExactlyOnce() throws InterruptedException {
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
                Thread t1 = new Thread(api::shutdown);
                Thread t2 = new Thread(api::shutdown);
                Thread t3 = new Thread(api::shutdown);

                t1.start();
                t2.start();
                t3.start();

                t1.join();
                t2.join();
                t3.join();

                // INVARIANT: Provider shutdown must be called exactly once
                assertThat(shutdownCount.get())
                    .as("Provider.shutdown() should be called exactly once regardless of thread interleaving")
                    .isEqualTo(1);
            }
        }
    }

    /**
     * Test: When setProvider and shutdown race, either:
     * - setProvider succeeds (runs before shutdown sets isShuttingDown flag)
     * - setProvider throws IllegalStateException (runs after shutdown sets flag)
     *
     * In either case, the original provider should always be shut down.
     */
    @Test
    void setProviderDuringShutdown_eitherSucceedsOrThrows() throws InterruptedException {
        try (AllInterleavings allInterleavings =
                new AllInterleavings("setProvider racing with shutdown")) {
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

                Thread shutdownThread = new Thread(api::shutdown);
                Thread setProviderThread = new Thread(() -> {
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

                shutdownThread.start();
                setProviderThread.start();

                shutdownThread.join();
                setProviderThread.join();

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

    /**
     * Test: Multiple providers registered to different domains should all be
     * shut down exactly once when shutdown() is called concurrently.
     */
    @Test
    void concurrentShutdown_allDomainProvidersShutdownExactlyOnce() throws InterruptedException {
        try (AllInterleavings allInterleavings =
                new AllInterleavings("Concurrent shutdown - all domain providers")) {
            while (allInterleavings.hasNext()) {
                AtomicInteger defaultShutdownCount = new AtomicInteger(0);
                AtomicInteger domain1ShutdownCount = new AtomicInteger(0);
                AtomicInteger domain2ShutdownCount = new AtomicInteger(0);

                FeatureProvider defaultProvider = createMockedProvider("default", defaultShutdownCount);
                FeatureProvider domain1Provider = createMockedProvider("domain1", domain1ShutdownCount);
                FeatureProvider domain2Provider = createMockedProvider("domain2", domain2ShutdownCount);

                OpenFeatureAPI api = OpenFeatureAPITestUtil.createAPI();

                // Register providers to different domains
                api.setProviderAndWait(defaultProvider);
                api.setProviderAndWait("domain1", domain1Provider);
                api.setProviderAndWait("domain2", domain2Provider);

                // Run concurrent shutdowns
                Thread t1 = new Thread(api::shutdown);
                Thread t2 = new Thread(api::shutdown);

                t1.start();
                t2.start();

                t1.join();
                t2.join();

                // INVARIANT: Each provider shut down exactly once
                assertThat(defaultShutdownCount.get())
                    .as("Default provider shutdown count")
                    .isEqualTo(1);
                assertThat(domain1ShutdownCount.get())
                    .as("Domain1 provider shutdown count")
                    .isEqualTo(1);
                assertThat(domain2ShutdownCount.get())
                    .as("Domain2 provider shutdown count")
                    .isEqualTo(1);
            }
        }
    }
}
