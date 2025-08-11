package dev.openfeature.sdk.multiProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.multiprovider.FirstMatchStrategy;
import org.junit.jupiter.api.Test;

class FirstMatchStrategyTest extends BaseStrategyTest {

    private final FirstMatchStrategy strategy = new FirstMatchStrategy();

    @Test
    void shouldSkipFlagNotFoundAndReturnFirstMatch() {
        setupProviderFlagNotFound(mockProvider1);
        setupProviderSuccess(mockProvider2, "success");

        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertNotNull(result);
        assertEquals("success", result.getValue());
        assertNull(result.getErrorCode());
    }

    @Test
    void shouldReturnFirstNonFlagNotFoundError() {
        setupProviderError(mockProvider1, ErrorCode.PARSE_ERROR);
        setupProviderSuccess(mockProvider2, "success");
        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));
        assertEquals(ErrorCode.PARSE_ERROR, result.getErrorCode());
    }

    @Test
    void shouldReturnSuccessWhenFirstProviderSucceeds() {
        setupProviderSuccess(mockProvider1, "first-success");
        setupProviderFlagNotFound(mockProvider2);
        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertNotNull(result);
        assertEquals("first-success", result.getValue());
        assertNull(result.getErrorCode());
    }

    @Test
    void shouldThrowFlagNotFoundWhenAllProvidersReturnFlagNotFound() {
        setupProviderFlagNotFound(mockProvider1);
        setupProviderFlagNotFound(mockProvider2);
        setupProviderFlagNotFound(mockProvider3);
        ProviderEvaluation<String> providerEvaluation = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, providerEvaluation.getErrorCode());
        assertEquals("No provider successfully responded", providerEvaluation.getErrorMessage());
    }

    @Test
    void shouldSkipMultipleFlagNotFoundAndReturnFirstOtherError() {
        setupProviderFlagNotFound(mockProvider1);
        setupProviderFlagNotFound(mockProvider2);
        setupProviderError(mockProvider3, ErrorCode.PARSE_ERROR);
        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));
        assertNotNull(result);
        assertEquals(ErrorCode.PARSE_ERROR, result.getErrorCode());
    }
}
