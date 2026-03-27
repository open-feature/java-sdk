package dev.openfeature.sdk.multiprovider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.exceptions.GeneralError;
import java.util.List;
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
        // Successful results should NOT be MultiProviderEvaluation
        assertTrue(!(result instanceof MultiProviderEvaluation));
    }

    @Test
    void shouldReturnMultiProviderEvaluationWithProviderDetailsWhenAllProvidersFail() {
        setupProviderErrorWithMessage(mockProvider1, ErrorCode.FLAG_NOT_FOUND, "flag missing");
        setupProviderErrorWithMessage(mockProvider2, ErrorCode.PARSE_ERROR, "parse failed");
        setupProviderErrorWithMessage(mockProvider3, ErrorCode.TYPE_MISMATCH, "type mismatch");
        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("No provider successfully responded"));

        MultiProviderEvaluation<String> multiResult = assertInstanceOf(MultiProviderEvaluation.class, result);
        List<ProviderError> errors = multiResult.getProviderErrors();
        assertNotNull(errors);
        assertEquals(3, errors.size());

        assertEquals("provider1", errors.get(0).getProviderName());
        assertEquals(ErrorCode.FLAG_NOT_FOUND, errors.get(0).getErrorCode());
        assertEquals("flag missing", errors.get(0).getErrorMessage());

        assertEquals("provider2", errors.get(1).getProviderName());
        assertEquals(ErrorCode.PARSE_ERROR, errors.get(1).getErrorCode());
        assertEquals("parse failed", errors.get(1).getErrorMessage());

        assertEquals("provider3", errors.get(2).getProviderName());
        assertEquals(ErrorCode.TYPE_MISMATCH, errors.get(2).getErrorCode());
        assertEquals("type mismatch", errors.get(2).getErrorMessage());
    }

    @Test
    void shouldSkipProvidersThatOnlyReturnErrors() {
        setupProviderError(mockProvider1, ErrorCode.INVALID_CONTEXT);
        setupProviderError(mockProvider2, ErrorCode.PROVIDER_NOT_READY);
        setupProviderError(mockProvider3, ErrorCode.GENERAL);

        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("No provider successfully responded"));

        MultiProviderEvaluation<String> multiResult = assertInstanceOf(MultiProviderEvaluation.class, result);
        List<ProviderError> errors = multiResult.getProviderErrors();
        assertNotNull(errors);
        assertEquals(3, errors.size());
        assertEquals(ErrorCode.INVALID_CONTEXT, errors.get(0).getErrorCode());
        assertEquals(ErrorCode.PROVIDER_NOT_READY, errors.get(1).getErrorCode());
        assertEquals(ErrorCode.GENERAL, errors.get(2).getErrorCode());
    }

    @Test
    void shouldCaptureExceptionDetailsFromThrowingProviders() {
        setupProviderException(mockProvider1, new GeneralError("connection timeout"));
        setupProviderErrorWithMessage(mockProvider2, ErrorCode.PARSE_ERROR, "bad json");
        setupProviderException(mockProvider3, new RuntimeException("unexpected failure"));

        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());

        MultiProviderEvaluation<String> multiResult = assertInstanceOf(MultiProviderEvaluation.class, result);
        List<ProviderError> errors = multiResult.getProviderErrors();
        assertNotNull(errors);
        assertEquals(3, errors.size());

        // First provider threw GeneralError
        assertEquals("provider1", errors.get(0).getProviderName());
        assertEquals(ErrorCode.GENERAL, errors.get(0).getErrorCode());
        assertEquals("connection timeout", errors.get(0).getErrorMessage());
        assertNotNull(errors.get(0).getException());

        // Second provider returned error-coded result
        assertEquals("provider2", errors.get(1).getProviderName());
        assertEquals(ErrorCode.PARSE_ERROR, errors.get(1).getErrorCode());
        assertEquals("bad json", errors.get(1).getErrorMessage());
        assertNull(errors.get(1).getException());

        // Third provider threw RuntimeException (non-OpenFeatureError)
        assertEquals("provider3", errors.get(2).getProviderName());
        assertEquals(ErrorCode.GENERAL, errors.get(2).getErrorCode());
        assertEquals("unexpected failure", errors.get(2).getErrorMessage());
        assertNotNull(errors.get(2).getException());
    }

    @Test
    void shouldReturnMultiProviderEvaluationForNonExistentFlag() {
        orderedProviders.clear();
        orderedProviders.put("old-provider", inMemoryProvider1);
        orderedProviders.put("new-provider", inMemoryProvider2);
        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertEquals(ErrorCode.GENERAL, result.getErrorCode());
        assertTrue(result.getErrorMessage().contains("No provider successfully responded"));

        // InMemoryProvider throws FlagNotFoundError, which should be captured
        MultiProviderEvaluation<String> multiResult = assertInstanceOf(MultiProviderEvaluation.class, result);
        List<ProviderError> errors = multiResult.getProviderErrors();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("old-provider", errors.get(0).getProviderName());
        assertEquals("new-provider", errors.get(1).getProviderName());
    }

    @Test
    void shouldReturnFirstSuccessEvenAfterErrors() {
        setupProviderError(mockProvider1, ErrorCode.PARSE_ERROR);
        setupProviderException(mockProvider2, new GeneralError("timeout"));
        setupProviderSuccess(mockProvider3, "finally-success");

        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        assertNotNull(result);
        assertEquals("finally-success", result.getValue());
        assertNull(result.getErrorCode());
        assertTrue(!(result instanceof MultiProviderEvaluation));
    }

    @Test
    void shouldIncludeProviderErrorDetailsInErrorMessage() {
        setupProviderErrorWithMessage(mockProvider1, ErrorCode.PARSE_ERROR, "parse failed");
        setupProviderErrorWithMessage(mockProvider2, ErrorCode.GENERAL, "timeout");
        setupProviderErrorWithMessage(mockProvider3, ErrorCode.FLAG_NOT_FOUND, "not found");

        ProviderEvaluation<String> result = strategy.evaluate(
                orderedProviders,
                FLAG_KEY,
                DEFAULT_STRING,
                null,
                p -> p.getStringEvaluation(FLAG_KEY, DEFAULT_STRING, null));

        String message = result.getErrorMessage();
        assertTrue(message.contains("provider1"));
        assertTrue(message.contains("PARSE_ERROR"));
        assertTrue(message.contains("provider2"));
        assertTrue(message.contains("GENERAL"));
        assertTrue(message.contains("provider3"));
        assertTrue(message.contains("FLAG_NOT_FOUND"));
    }
}
