package dev.openfeature.sdk.multiProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.multiprovider.FirstSuccessfulStrategy;
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
        GeneralError exception = assertThrows(GeneralError.class, () -> {
            strategy.evaluate(
                    orderedProviders,
                    FLAG_KEY,
                    DEFAULT_STRING,
                    null,
                    p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));
        });

        assertEquals("evaluation error", exception.getMessage());
    }

    @Test
    void shouldSkipProvidersThatOnlyReturnErrors() {
        setupProviderError(mockProvider1, ErrorCode.INVALID_CONTEXT);
        setupProviderError(mockProvider2, ErrorCode.PROVIDER_NOT_READY);
        setupProviderError(mockProvider3, ErrorCode.GENERAL);

        assertThrows(GeneralError.class, () -> {
            strategy.evaluate(
                    orderedProviders,
                    FLAG_KEY,
                    DEFAULT_STRING,
                    null,
                    p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));
        });
    }

    @Test
    void shouldThrowGeneralErrorForNonExistentFlag() {
        orderedProviders.clear();
        orderedProviders.put("old-provider", inMemoryProvider1);
        orderedProviders.put("new-provider", inMemoryProvider2);
        assertThrows(GeneralError.class, () -> {
            strategy.evaluate(
                    orderedProviders,
                    "non-existent-flag",
                    DEFAULT_STRING,
                    null,
                    p -> p.getStringEvaluation("non-existent-flag", DEFAULT_STRING, null));
        });
    }
}
