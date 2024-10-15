package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlagEvaluationDetailsTest {

    @Test
    @DisplayName("Should have empty constructor")
    public void empty() {
        FlagEvaluationDetails<Integer> details = new FlagEvaluationDetails<Integer>();
        assertNotNull(details);
    }

    @Test
    @DisplayName("Should have flagKey, value, variant, reason, errorCode, errorMessage, metadata constructor")
    // removeing this constructor is a breaking change!
    public void sevenArgConstructor() {

        String flagKey = "my-flag";
        Integer value = 100;
        String variant = "1-hundred";
        Reason reason = Reason.DEFAULT;
        ErrorCode errorCode = ErrorCode.GENERAL;
        String errorMessage = "message";
        ImmutableMetadata metadata = ImmutableMetadata.builder().build();

        FlagEvaluationDetails<Integer> details = new FlagEvaluationDetails<>(
                flagKey,
                value,
                variant,
                reason.toString(),
                errorCode,
                errorMessage,
                metadata);

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
                .flagMetadata(ImmutableMetadata.builder().addString("metadata", "1").build())
                .build();

        FlagEvaluationDetails fed2 = FlagEvaluationDetails.builder()
                .reason(Reason.ERROR.toString())
                .value(false)
                .errorCode(ErrorCode.GENERAL)
                .errorMessage("error XXX")
                .flagMetadata(ImmutableMetadata.builder().addString("metadata", "1").build())
                .build();

        assertEquals(fed1, fed2);
    }
}
