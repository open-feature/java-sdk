package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlagEvaluationDetailsTest {

    @Test
    @DisplayName("Should create empty evaluation details with builder")
    public void empty() {
        FlagEvaluationDetails<Integer> details =
                FlagEvaluationDetails.<Integer>builder().build();
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
        ImmutableMetadata metadata = ImmutableMetadata.builder().build();

        FlagEvaluationDetails<Integer> details = FlagEvaluationDetails.<Integer>builder()
                .flagKey(flagKey)
                .value(value)
                .variant(variant)
                .reason(reason.toString())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .flagMetadata(metadata)
                .build();

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
        FlagEvaluationDetails fed1 = FlagEvaluationDetails.builder()
                .reason(Reason.ERROR.toString())
                .value(false)
                .errorCode(ErrorCode.GENERAL)
                .errorMessage("error XXX")
                .flagMetadata(
                        ImmutableMetadata.builder().addString("metadata", "1").build())
                .build();

        FlagEvaluationDetails fed2 = FlagEvaluationDetails.builder()
                .reason(Reason.ERROR.toString())
                .value(false)
                .errorCode(ErrorCode.GENERAL)
                .errorMessage("error XXX")
                .flagMetadata(
                        ImmutableMetadata.builder().addString("metadata", "1").build())
                .build();

        assertEquals(fed1, fed2);
    }
}
