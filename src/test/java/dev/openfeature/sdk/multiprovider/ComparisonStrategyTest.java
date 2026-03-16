package dev.openfeature.sdk.multiprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ComparisonStrategyTest extends BaseStrategyTest {

    @Test
    void shouldReturnFallbackResultWhenAllProvidersAgree() {
        setupProviderSuccess(mockProvider1, "same");
        setupProviderSuccess(mockProvider2, "same");

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        ComparisonStrategy strategy = new ComparisonStrategy("provider2");
        ProviderEvaluation<String> result = strategy.evaluate(
                providers,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertNotNull(result);
        assertEquals("same", result.getValue());
        assertNull(result.getErrorCode());
    }

    @Test
    void shouldCallMismatchCallbackAndReturnFallbackResult() {
        setupProviderSuccess(mockProvider1, "first");
        setupProviderSuccess(mockProvider2, "second");

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        AtomicInteger callbackCount = new AtomicInteger();
        ComparisonStrategy strategy = new ComparisonStrategy(
                "provider2",
                (key, evaluations) -> callbackCount.incrementAndGet());

        ProviderEvaluation<String> result = strategy.evaluate(
                providers,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals("second", result.getValue());
        assertNull(result.getErrorCode());
        assertEquals(1, callbackCount.get());
    }

    @Test
    void shouldReturnGeneralErrorWhenAnyProviderFails() {
        setupProviderSuccess(mockProvider1, "ok");
        setupProviderError(mockProvider2, ErrorCode.PARSE_ERROR);

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        ComparisonStrategy strategy = new ComparisonStrategy("provider1");
        ProviderEvaluation<String> result = strategy.evaluate(
                providers,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("provider2"));
    }

    @Test
    void shouldThrowWhenFallbackProviderIsMissing() {
        setupProviderSuccess(mockProvider1, "ok");

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);

        ComparisonStrategy strategy = new ComparisonStrategy("provider2");
        assertThrows(
                IllegalArgumentException.class,
                () -> strategy.evaluate(
                        providers,
                        FLAG_KEY,
                        DEFAULT_STRING,
                        null,
                        p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null)));
    }

    @Test
    void shouldEvaluateProvidersConcurrently() throws InterruptedException {
        // Use a latch to prove that providers run in parallel:
        // both providers block on the latch, so they must be on
        // separate threads for the test to complete.
        CountDownLatch bothStarted = new CountDownLatch(2);
        CountDownLatch proceed = new CountDownLatch(1);
        Set<String> threadNames = ConcurrentHashMap.newKeySet();

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        setupProviderSuccess(mockProvider1, "val");
        setupProviderSuccess(mockProvider2, "val");

        ComparisonStrategy strategy = new ComparisonStrategy("provider1");
        ProviderEvaluation<String> result = strategy.evaluate(
                providers,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                provider -> {
                    threadNames.add(Thread.currentThread().getName());
                    bothStarted.countDown();
                    try {
                        // Wait for both providers to signal they've started
                        bothStarted.await(5, TimeUnit.SECONDS);
                        proceed.countDown();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return provider.getStringEvaluation(
                            FLAG_KEY, DEFAULT_STRING, null);
                });

        assertNotNull(result);
        assertEquals("val", result.getValue());
        assertNull(result.getErrorCode());
        // Verify that at least 2 different threads were used
        assertTrue(
                threadNames.size() >= 2,
                "Expected concurrent execution on multiple threads, "
                        + "but only saw: " + threadNames);
    }

    @Test
    void shouldReuseProvidedExecutorService() {
        ExecutorService customExecutor = Executors.newFixedThreadPool(2);
        try {
            setupProviderSuccess(mockProvider1, "ok");
            setupProviderSuccess(mockProvider2, "ok");

            Map<String, FeatureProvider> providers = new LinkedHashMap<>();
            providers.put("provider1", mockProvider1);
            providers.put("provider2", mockProvider2);

            ComparisonStrategy strategy = new ComparisonStrategy(
                    "provider1", null, customExecutor, 5000);

            // Execute twice to confirm the same executor is reused
            for (int i = 0; i < 2; i++) {
                ProviderEvaluation<String> result = strategy.evaluate(
                        providers,
                        FLAG_KEY,
                        DEFAULT_STRING,
                        null,
                        p -> p.getStringEvaluation(
                                FLAG_KEY, DEFAULT_STRING, null));
                assertNotNull(result);
                assertEquals("ok", result.getValue());
                assertNull(result.getErrorCode());
            }

            // Executor should still be usable (not shut down)
            assertTrue(
                    !customExecutor.isShutdown(),
                    "Executor should not be shut down by the strategy");
        } finally {
            customExecutor.shutdown();
        }
    }

    @Test
    void shouldCollectAllProviderErrorsWhenMultipleFail() {
        setupProviderError(mockProvider1, ErrorCode.PARSE_ERROR);
        setupProviderError(mockProvider2, ErrorCode.FLAG_NOT_FOUND);

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        ComparisonStrategy strategy = new ComparisonStrategy("provider1");
        ProviderEvaluation<String> result = strategy.evaluate(
                providers,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertTrue(
                result.getErrorMessage().contains("provider1"),
                "Error should mention provider1");
        assertTrue(
                result.getErrorMessage().contains("provider2"),
                "Error should mention provider2");
    }
}
