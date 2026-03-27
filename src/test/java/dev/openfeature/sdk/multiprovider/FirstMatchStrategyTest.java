package dev.openfeature.sdk.multiprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import java.util.List;
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
    void shouldReturnMultiProviderEvaluationWhenAllProvidersReturnFlagNotFound() {
        setupProviderFlagNotFound(mockProvider1);
        setupProviderFlagNotFound(mockProvider2);
        setupProviderFlagNotFound(mockProvider3);
        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.FLAG_NOT_FOUND, result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("Flag not found in any provider"));

        MultiProviderEvaluation<String> multiResult = assertInstanceOf(MultiProviderEvaluation.class, result);
        List<ProviderError> errors = multiResult.getProviderErrors();
        assertNotNull(errors);
        assertEquals(3, errors.size());
        assertEquals("provider1", errors.get(0).getProviderName());
        assertEquals(ErrorCode.FLAG_NOT_FOUND, errors.get(0).getErrorCode());
        assertEquals("provider2", errors.get(1).getProviderName());
        assertEquals(ErrorCode.FLAG_NOT_FOUND, errors.get(1).getErrorCode());
        assertEquals("provider3", errors.get(2).getProviderName());
        assertEquals(ErrorCode.FLAG_NOT_FOUND, errors.get(2).getErrorCode());
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

    @Test
    void shouldCaptureThrownFlagNotFoundErrorsAsProviderErrors() {
        setupProviderException(mockProvider1, new FlagNotFoundError("not in provider1"));
        setupProviderException(mockProvider2, new FlagNotFoundError("not in provider2"));
        setupProviderException(mockProvider3, new FlagNotFoundError("not in provider3"));

        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.FLAG_NOT_FOUND, result.getErrorCode());

        MultiProviderEvaluation<String> multiResult = assertInstanceOf(MultiProviderEvaluation.class, result);
        List<ProviderError> errors = multiResult.getProviderErrors();
        assertNotNull(errors);
        assertEquals(3, errors.size());

        assertEquals("provider1", errors.get(0).getProviderName());
        assertEquals(ErrorCode.FLAG_NOT_FOUND, errors.get(0).getErrorCode());
        assertEquals("not in provider1", errors.get(0).getErrorMessage());
        assertNotNull(errors.get(0).getException());

        assertEquals("provider2", errors.get(1).getProviderName());
        assertEquals("provider3", errors.get(2).getProviderName());
    }

    @Test
    void shouldIncludeProviderNamesInAggregateErrorMessage() {
        setupProviderFlagNotFound(mockProvider1);
        setupProviderFlagNotFound(mockProvider2);
        setupProviderFlagNotFound(mockProvider3);

        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        String message = result.getErrorMessage();
        assertTrue(message.contains("provider1"));
        assertTrue(message.contains("provider2"));
        assertTrue(message.contains("provider3"));
        assertTrue(message.contains("FLAG_NOT_FOUND"));
    }
}
