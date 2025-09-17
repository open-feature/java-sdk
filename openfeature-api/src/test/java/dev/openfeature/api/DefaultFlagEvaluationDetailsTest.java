package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultFlagEvaluationDetailsTest {

    @Test
    @DisplayName("Should create empty evaluation details with builder")
    public void empty() {
        FlagEvaluationDetails<Integer> details = new DefaultFlagEvaluationDetails<>();
        assertNotNull(details);
    }

    @Test
    @DisplayName("Should create evaluation details with all fields using builder")
    public void builderWithAllFields() {

        String flagKey = "my-flag";
        Integer value = 100;
        String variant = "1-hundred";
        Reason reason = Reason.DEFAULT;
        ErrorCode errorCode = ErrorCode.GENERAL;
        String errorMessage = "message";
        Metadata metadata = Metadata.EMPTY;

        FlagEvaluationDetails<Integer> details = new DefaultFlagEvaluationDetails<>(
                flagKey, value, variant, reason.toString(), errorCode, errorMessage, metadata);

        assertEquals(flagKey, details.getFlagKey());
        assertEquals(value, details.getValue());
        assertEquals(variant, details.getVariant());
        assertEquals(reason.toString(), details.getReason());
        assertEquals(errorCode, details.getErrorCode());
        assertEquals(errorMessage, details.getErrorMessage());
        assertEquals(metadata, details.getFlagMetadata());
    }

    @Test
    @DisplayName("should be able to compare 2 FlagEvaluationDetails")
    public void compareFlagEvaluationDetails() {
        String flagKey = "my-flag";
        FlagEvaluationDetails fed1 = new DefaultFlagEvaluationDetails<>(
                flagKey,
                false,
                null,
                null,
                ErrorCode.GENERAL,
                "error XXX",
                Metadata.immutableBuilder().add("metadata", "1").build());

        FlagEvaluationDetails fed2 = new DefaultFlagEvaluationDetails<>(
                flagKey,
                false,
                null,
                null,
                ErrorCode.GENERAL,
                "error XXX",
                Metadata.immutableBuilder().add("metadata", "1").build());

        assertEquals(fed1, fed2);
    }
}
