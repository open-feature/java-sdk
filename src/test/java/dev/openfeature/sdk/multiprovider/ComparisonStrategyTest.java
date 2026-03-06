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
        ComparisonStrategy strategy = new ComparisonStrategy("provider2", (key, evaluations) -> callbackCount.incrementAndGet());

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
}
