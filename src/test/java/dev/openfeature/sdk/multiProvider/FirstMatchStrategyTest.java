package dev.openfeature.sdk.multiProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
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

        assertNotNull(result);
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
        FlagNotFoundError exception = assertThrows(FlagNotFoundError.class, () -> {
            strategy.evaluate(
                    orderedProviders,
                    FLAG_KEY,
                    DEFAULT_STRING,
                    null,
                    p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));
        });

        assertEquals("flag not found", exception.getMessage());
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
