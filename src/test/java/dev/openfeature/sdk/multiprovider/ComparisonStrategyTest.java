package dev.openfeature.sdk.multiprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderEvaluation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
                providers, FLAG_KEY, DEFAULT_STRING, null, p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

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
        ComparisonStrategy strategy =
                new ComparisonStrategy("provider2", (key, evaluations) -> callbackCount.incrementAndGet());

        ProviderEvaluation<String> result = strategy.evaluate(
                providers, FLAG_KEY, DEFAULT_STRING, null, p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

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
                providers, FLAG_KEY, DEFAULT_STRING, null, p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("provider2"));

        List<ProviderError> providerErrors = ((MultiProviderEvaluation<String>) result).getProviderErrors();
        assertEquals(1, providerErrors.size());
        assertEquals("provider2", providerErrors.get(0).getProviderName());
        assertEquals(ErrorCode.PARSE_ERROR, providerErrors.get(0).getErrorCode());
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
    void shouldEvaluateProvidersConcurrently() {
        // Use a latch to prove that providers run in parallel:
        // both providers block on the latch, so they must be on
        // separate threads for the test to complete.
        CountDownLatch bothStarted = new CountDownLatch(2);
        Set<String> threadNames = ConcurrentHashMap.newKeySet();

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        setupProviderSuccess(mockProvider1, "val");
        setupProviderSuccess(mockProvider2, "val");

        // A dedicated pool of two threads: the default ForkJoinPool.commonPool() can have a
        // parallelism of 1 on single-core runners, which would make this assertion flaky.
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            ComparisonStrategy strategy = new ComparisonStrategy("provider1", null, executor, 5_000);
            ProviderEvaluation<String> result =
                    strategy.evaluate(providers, FLAG_KEY, DEFAULT_STRING, null, provider -> {
                        threadNames.add(Thread.currentThread().getName());
                        bothStarted.countDown();
                        try {
                            // Wait for both providers to signal they've started
                            bothStarted.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return provider.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null);
                    });

            assertNotNull(result);
            assertEquals("val", result.getValue());
            assertNull(result.getErrorCode());
            // Verify that at least 2 different threads were used
            assertTrue(
                    threadNames.size() >= 2,
                    "Expected concurrent execution on multiple threads, but only saw: " + threadNames);
        } finally {
            executor.shutdownNow();
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
                providers, FLAG_KEY, DEFAULT_STRING, null, p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("provider1"), "Error should mention provider1");
        assertTrue(result.getErrorMessage().contains("provider2"), "Error should mention provider2");

        // Errors follow the provider registration order, not the internal concurrent map order.
        List<ProviderError> providerErrors = ((MultiProviderEvaluation<String>) result).getProviderErrors();
        assertEquals(2, providerErrors.size());
        assertEquals("provider1", providerErrors.get(0).getProviderName());
        assertEquals(ErrorCode.PARSE_ERROR, providerErrors.get(0).getErrorCode());
        assertEquals("provider2", providerErrors.get(1).getProviderName());
        assertEquals(ErrorCode.FLAG_NOT_FOUND, providerErrors.get(1).getErrorCode());
    }

    @Test
    void shouldPassSuccessfulEvaluationsInRegistrationOrderToMismatchCallback() {
        setupProviderSuccess(mockProvider1, "first");
        setupProviderSuccess(mockProvider2, "second");

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        AtomicReference<Map<String, ProviderEvaluation<?>>> captured = new AtomicReference<>();
        ComparisonStrategy strategy =
                new ComparisonStrategy("provider2", (key, evaluations) -> captured.set(evaluations));

        strategy.evaluate(
                providers, FLAG_KEY, DEFAULT_STRING, null, p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertNotNull(captured.get());
        assertEquals(
                List.of("provider1", "provider2"), List.copyOf(captured.get().keySet()));
    }

    @Test
    void shouldReturnErrorWhenNoProvidersConfigured() {
        ComparisonStrategy strategy = new ComparisonStrategy("provider1");
        ProviderEvaluation<String> result = strategy.evaluate(
                new LinkedHashMap<>(),
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertEquals("No providers configured", result.getErrorMessage());
    }

    @Test
    void shouldRecordThrownProviderExceptionAsProviderError() {
        setupProviderSuccess(mockProvider1, "ok");
        setupProviderException(mockProvider2, new IllegalStateException("provider blew up"));

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        ComparisonStrategy strategy = new ComparisonStrategy("provider1");
        ProviderEvaluation<String> result = strategy.evaluate(
                providers, FLAG_KEY, DEFAULT_STRING, null, p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        List<ProviderError> providerErrors = ((MultiProviderEvaluation<String>) result).getProviderErrors();
        assertEquals(1, providerErrors.size());
        assertEquals("provider2", providerErrors.get(0).getProviderName());
        assertEquals("provider blew up", providerErrors.get(0).getErrorMessage());
        assertNotNull(providerErrors.get(0).getException());
    }

    @Test
    void shouldTreatNullEvaluationAsProviderError() {
        setupProviderSuccess(mockProvider1, "ok");
        when(mockProvider2.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null)).thenReturn(null);

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        ComparisonStrategy strategy = new ComparisonStrategy("provider1");
        ProviderEvaluation<String> result = strategy.evaluate(
                providers, FLAG_KEY, DEFAULT_STRING, null, p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        List<ProviderError> providerErrors = ((MultiProviderEvaluation<String>) result).getProviderErrors();
        assertEquals(1, providerErrors.size());
        assertEquals("null evaluation", providerErrors.get(0).getErrorMessage());
    }

    @Test
    void shouldReturnTimeoutErrorWhenProvidersExceedTheTimeout() {
        setupProviderSuccess(mockProvider1, "ok");
        setupProviderSuccess(mockProvider2, "ok");

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", mockProvider1);
        providers.put("provider2", mockProvider2);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            ComparisonStrategy strategy = new ComparisonStrategy("provider1", null, executor, 50);
            ProviderEvaluation<String> result =
                    strategy.evaluate(providers, FLAG_KEY, DEFAULT_STRING, null, provider -> {
                        try {
                            Thread.sleep(5_000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return provider.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null);
                    });

            assertEquals(ErrorCode.GENERAL, result.getErrorCode());
            assertTrue(
                    result.getErrorMessage().contains("timed out after 50ms"),
                    "Expected a timeout message, got: " + result.getErrorMessage());
        } finally {
            executor.shutdownNow();
        }
    }
}
