package dev.openfeature.sdk.multiprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ProviderEvaluation;
import org.junit.jupiter.api.Test;

class FirstSuccessfulStrategyTest extends BaseStrategyTest {

    private final FirstSuccessfulStrategy strategy = new FirstSuccessfulStrategy();

    @Test
    void shouldSkipFlagNotFoundAndReturnFirstSuccess() {
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
    void shouldThrowGeneralErrorWhenAllProvidersFail() {
        setupProviderFlagNotFound(mockProvider1);
        setupProviderError(mockProvider2, ErrorCode.PARSE_ERROR);
        setupProviderError(mockProvider3, ErrorCode.TYPE_MISMATCH);
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
    void shouldSkipProvidersThatOnlyReturnErrors() {
        setupProviderError(mockProvider1, ErrorCode.INVALID_CONTEXT);
        setupProviderError(mockProvider2, ErrorCode.PROVIDER_NOT_READY);
        setupProviderError(mockProvider3, ErrorCode.GENERAL);

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
    void shouldThrowGeneralErrorForNonExistentFlag() {
        orderedProviders.clear();
        orderedProviders.add(inMemoryProvider1);
        orderedProviders.add(inMemoryProvider2);
        ProviderEvaluation<String> providerEvaluation = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, providerEvaluation.getErrorCode());
        assertEquals("No provider successfully responded", providerEvaluation.getErrorMessage());
    }
}
