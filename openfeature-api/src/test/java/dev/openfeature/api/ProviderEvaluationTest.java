package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProviderEvaluationTest {

    @Test
    @DisplayName("Should create empty evaluation with builder")
    public void empty() {
        ProviderEvaluation<Integer> details =
                ProviderEvaluation.<Integer>builder().build();
        assertNotNull(details);
    }

    @Test
    @DisplayName("Should create evaluation with all fields using builder")
    public void builderWithAllFields() {

        Integer value = 100;
        String variant = "1-hundred";
        Reason reason = Reason.DEFAULT;
        ErrorCode errorCode = ErrorCode.GENERAL;
        String errorMessage = "message";
        ImmutableMetadata metadata = ImmutableMetadata.builder().build();

        ProviderEvaluation<Integer> details = ProviderEvaluation.<Integer>builder()
                .value(value)
                .variant(variant)
                .reason(reason.toString())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .flagMetadata(metadata)
                .build();

        assertEquals(value, details.getValue());
        assertEquals(variant, details.getVariant());
        assertEquals(reason.toString(), details.getReason());
        assertEquals(errorCode, details.getErrorCode());
        assertEquals(errorMessage, details.getErrorMessage());
        assertEquals(metadata, details.getFlagMetadata());
    }
}
