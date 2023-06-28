package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProviderEvaluationTest {

    @Test
    @DisplayName("Should have empty constructor")
    public void empty() {
        ProviderEvaluation<Integer> details = new ProviderEvaluation<Integer>();
        assertNotNull(details);
    }

    @Test
    @DisplayName("Should have value, variant, reason, errorCode, errorMessage, metadata constructor")
    // removeing this constructor is a breaking change!
    public void sixArgConstructor() {

        Integer value = 100;
        String variant = "1-hundred";
        Reason reason = Reason.DEFAULT;
        ErrorCode errorCode = ErrorCode.GENERAL;
        String errorMessage = "message";
        ImmutableMetadata metadata = ImmutableMetadata.builder().build();

        ProviderEvaluation<Integer> details = new ProviderEvaluation<>(
        value,
        variant,
        reason.toString(),
        errorCode,
        errorMessage,
        metadata);

        assertEquals(value, details.getValue());
        assertEquals(variant, details.getVariant());
        assertEquals(reason.toString(), details.getReason());
        assertEquals(errorCode, details.getErrorCode());
        assertEquals(errorMessage, details.getErrorMessage());
        assertEquals(metadata, details.getFlagMetadata());
    }
}
