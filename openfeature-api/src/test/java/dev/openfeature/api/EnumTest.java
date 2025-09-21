package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for all OpenFeature API enum classes.
 * Tests enum values, completeness, and special behaviors.
 */
class EnumTest {

    // ErrorCode enum tests
    @Test
    void errorCode_shouldHaveAllExpectedValues() {
        ErrorCode[] values = ErrorCode.values();
        assertEquals(8, values.length);

        // Verify all expected values exist
        Set<String> expectedValues = Set.of(
                "PROVIDER_NOT_READY",
                "FLAG_NOT_FOUND",
                "PARSE_ERROR",
                "TYPE_MISMATCH",
                "TARGETING_KEY_MISSING",
                "INVALID_CONTEXT",
                "GENERAL",
                "PROVIDER_FATAL");

        Set<String> actualValues = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());

        assertEquals(expectedValues, actualValues);
    }

    @Test
    void errorCode_shouldSupportValueOfOperation() {
        // Test valueOf for each error code
        assertSame(ErrorCode.PROVIDER_NOT_READY, ErrorCode.valueOf("PROVIDER_NOT_READY"));
        assertSame(ErrorCode.FLAG_NOT_FOUND, ErrorCode.valueOf("FLAG_NOT_FOUND"));
        assertSame(ErrorCode.PARSE_ERROR, ErrorCode.valueOf("PARSE_ERROR"));
        assertSame(ErrorCode.TYPE_MISMATCH, ErrorCode.valueOf("TYPE_MISMATCH"));
        assertSame(ErrorCode.TARGETING_KEY_MISSING, ErrorCode.valueOf("TARGETING_KEY_MISSING"));
        assertSame(ErrorCode.INVALID_CONTEXT, ErrorCode.valueOf("INVALID_CONTEXT"));
        assertSame(ErrorCode.GENERAL, ErrorCode.valueOf("GENERAL"));
        assertSame(ErrorCode.PROVIDER_FATAL, ErrorCode.valueOf("PROVIDER_FATAL"));
    }

    @Test
    void errorCode_shouldHaveConsistentToString() {
        for (ErrorCode errorCode : ErrorCode.values()) {
            assertEquals(errorCode.name(), errorCode.toString());
        }
    }

    // FlagValueType enum tests
    @Test
    void flagValueType_shouldHaveAllExpectedValues() {
        FlagValueType[] values = FlagValueType.values();
        assertEquals(5, values.length);

        // Verify all expected values exist
        Set<String> expectedValues = Set.of("STRING", "INTEGER", "DOUBLE", "OBJECT", "BOOLEAN");

        Set<String> actualValues = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());

        assertEquals(expectedValues, actualValues);
    }

    @Test
    void flagValueType_shouldSupportValueOfOperation() {
        assertSame(FlagValueType.STRING, FlagValueType.valueOf("STRING"));
        assertSame(FlagValueType.INTEGER, FlagValueType.valueOf("INTEGER"));
        assertSame(FlagValueType.DOUBLE, FlagValueType.valueOf("DOUBLE"));
        assertSame(FlagValueType.OBJECT, FlagValueType.valueOf("OBJECT"));
        assertSame(FlagValueType.BOOLEAN, FlagValueType.valueOf("BOOLEAN"));
    }

    @Test
    void flagValueType_shouldCoverAllBasicTypes() {
        // Ensure we have types for all basic data types
        assertTrue(Arrays.asList(FlagValueType.values()).contains(FlagValueType.STRING));
        assertTrue(Arrays.asList(FlagValueType.values()).contains(FlagValueType.INTEGER));
        assertTrue(Arrays.asList(FlagValueType.values()).contains(FlagValueType.DOUBLE));
        assertTrue(Arrays.asList(FlagValueType.values()).contains(FlagValueType.BOOLEAN));
        assertTrue(Arrays.asList(FlagValueType.values()).contains(FlagValueType.OBJECT));
    }

    // ProviderEvent enum tests
    @Test
    void providerEvent_shouldHaveAllExpectedValues() {
        ProviderEvent[] values = ProviderEvent.values();
        assertEquals(4, values.length);

        // Verify all expected values exist
        Set<String> expectedValues =
                Set.of("PROVIDER_READY", "PROVIDER_CONFIGURATION_CHANGED", "PROVIDER_ERROR", "PROVIDER_STALE");

        Set<String> actualValues = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());

        assertEquals(expectedValues, actualValues);
    }

    @Test
    void providerEvent_shouldSupportValueOfOperation() {
        assertSame(ProviderEvent.PROVIDER_READY, ProviderEvent.valueOf("PROVIDER_READY"));
        assertSame(
                ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, ProviderEvent.valueOf("PROVIDER_CONFIGURATION_CHANGED"));
        assertSame(ProviderEvent.PROVIDER_ERROR, ProviderEvent.valueOf("PROVIDER_ERROR"));
        assertSame(ProviderEvent.PROVIDER_STALE, ProviderEvent.valueOf("PROVIDER_STALE"));
    }

    @Test
    void providerEvent_shouldRepresentProviderLifecycle() {
        // Events should represent the complete provider lifecycle
        assertTrue(Arrays.asList(ProviderEvent.values()).contains(ProviderEvent.PROVIDER_READY));
        assertTrue(Arrays.asList(ProviderEvent.values()).contains(ProviderEvent.PROVIDER_ERROR));
        assertTrue(Arrays.asList(ProviderEvent.values()).contains(ProviderEvent.PROVIDER_STALE));
        assertTrue(Arrays.asList(ProviderEvent.values()).contains(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED));
    }

    // ProviderState enum tests
    @Test
    void providerState_shouldHaveAllExpectedValues() {
        ProviderState[] values = ProviderState.values();
        assertEquals(5, values.length);

        // Verify all expected values exist
        Set<String> expectedValues = Set.of("READY", "NOT_READY", "ERROR", "STALE", "FATAL");

        Set<String> actualValues = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());

        assertEquals(expectedValues, actualValues);
    }

    @Test
    void providerState_shouldSupportValueOfOperation() {
        assertSame(ProviderState.READY, ProviderState.valueOf("READY"));
        assertSame(ProviderState.NOT_READY, ProviderState.valueOf("NOT_READY"));
        assertSame(ProviderState.ERROR, ProviderState.valueOf("ERROR"));
        assertSame(ProviderState.STALE, ProviderState.valueOf("STALE"));
        assertSame(ProviderState.FATAL, ProviderState.valueOf("FATAL"));
    }

    @Test
    void providerState_matchesEvent_shouldWorkCorrectly() {
        // Test positive matches
        assertTrue(ProviderState.READY.matchesEvent(ProviderEvent.PROVIDER_READY));
        assertTrue(ProviderState.STALE.matchesEvent(ProviderEvent.PROVIDER_STALE));
        assertTrue(ProviderState.ERROR.matchesEvent(ProviderEvent.PROVIDER_ERROR));

        // Test negative matches
        assertFalse(ProviderState.READY.matchesEvent(ProviderEvent.PROVIDER_ERROR));
        assertFalse(ProviderState.READY.matchesEvent(ProviderEvent.PROVIDER_STALE));
        assertFalse(ProviderState.READY.matchesEvent(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED));

        assertFalse(ProviderState.STALE.matchesEvent(ProviderEvent.PROVIDER_READY));
        assertFalse(ProviderState.STALE.matchesEvent(ProviderEvent.PROVIDER_ERROR));
        assertFalse(ProviderState.STALE.matchesEvent(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED));

        assertFalse(ProviderState.ERROR.matchesEvent(ProviderEvent.PROVIDER_READY));
        assertFalse(ProviderState.ERROR.matchesEvent(ProviderEvent.PROVIDER_STALE));
        assertFalse(ProviderState.ERROR.matchesEvent(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED));

        // Test states that don't match any event
        assertFalse(ProviderState.NOT_READY.matchesEvent(ProviderEvent.PROVIDER_READY));
        assertFalse(ProviderState.NOT_READY.matchesEvent(ProviderEvent.PROVIDER_ERROR));
        assertFalse(ProviderState.NOT_READY.matchesEvent(ProviderEvent.PROVIDER_STALE));
        assertFalse(ProviderState.NOT_READY.matchesEvent(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED));

        assertFalse(ProviderState.FATAL.matchesEvent(ProviderEvent.PROVIDER_READY));
        assertFalse(ProviderState.FATAL.matchesEvent(ProviderEvent.PROVIDER_ERROR));
        assertFalse(ProviderState.FATAL.matchesEvent(ProviderEvent.PROVIDER_STALE));
        assertFalse(ProviderState.FATAL.matchesEvent(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED));
    }

    @Test
    void providerState_matchesEvent_shouldHandleAllStatesAndEvents() {
        // Test that every combination is handled correctly
        for (ProviderState state : ProviderState.values()) {
            for (ProviderEvent event : ProviderEvent.values()) {
                boolean result = state.matchesEvent(event);

                // Verify the expected matches
                if ((state == ProviderState.READY && event == ProviderEvent.PROVIDER_READY)
                        || (state == ProviderState.STALE && event == ProviderEvent.PROVIDER_STALE)
                        || (state == ProviderState.ERROR && event == ProviderEvent.PROVIDER_ERROR)) {
                    assertTrue(result, "Expected " + state + " to match " + event);
                } else {
                    assertFalse(result, "Expected " + state + " NOT to match " + event);
                }
            }
        }
    }

    // Reason enum tests
    @Test
    void reason_shouldHaveAllExpectedValues() {
        Reason[] values = Reason.values();
        assertEquals(8, values.length);

        // Verify all expected values exist
        Set<String> expectedValues =
                Set.of("DISABLED", "SPLIT", "TARGETING_MATCH", "DEFAULT", "UNKNOWN", "CACHED", "STATIC", "ERROR");

        Set<String> actualValues = Arrays.stream(values).map(Enum::name).collect(Collectors.toSet());

        assertEquals(expectedValues, actualValues);
    }

    @Test
    void reason_shouldSupportValueOfOperation() {
        assertSame(Reason.DISABLED, Reason.valueOf("DISABLED"));
        assertSame(Reason.SPLIT, Reason.valueOf("SPLIT"));
        assertSame(Reason.TARGETING_MATCH, Reason.valueOf("TARGETING_MATCH"));
        assertSame(Reason.DEFAULT, Reason.valueOf("DEFAULT"));
        assertSame(Reason.UNKNOWN, Reason.valueOf("UNKNOWN"));
        assertSame(Reason.CACHED, Reason.valueOf("CACHED"));
        assertSame(Reason.STATIC, Reason.valueOf("STATIC"));
        assertSame(Reason.ERROR, Reason.valueOf("ERROR"));
    }

    @Test
    void reason_shouldCoverAllResolutionScenarios() {
        // Verify we have reasons for all typical flag resolution scenarios
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.TARGETING_MATCH)); // Feature targeting
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.SPLIT)); // A/B testing
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.DEFAULT)); // Default value used
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.DISABLED)); // Feature disabled
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.CACHED)); // Cached value
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.STATIC)); // Static value
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.ERROR)); // Error occurred
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.UNKNOWN)); // Unknown reason
    }

    // Cross-enum relationship tests
    @Test
    void enums_shouldHaveConsistentNamingConventions() {
        // All enum values should use uppercase with underscores
        for (ErrorCode value : ErrorCode.values()) {
            assertTrue(
                    value.name().matches("^[A-Z_]+$"), "ErrorCode " + value + " should be uppercase with underscores");
        }

        for (FlagValueType value : FlagValueType.values()) {
            assertTrue(
                    value.name().matches("^[A-Z_]+$"),
                    "FlagValueType " + value + " should be uppercase with underscores");
        }

        for (ProviderEvent value : ProviderEvent.values()) {
            assertTrue(
                    value.name().matches("^[A-Z_]+$"),
                    "ProviderEvent " + value + " should be uppercase with underscores");
        }

        for (ProviderState value : ProviderState.values()) {
            assertTrue(
                    value.name().matches("^[A-Z_]+$"),
                    "ProviderState " + value + " should be uppercase with underscores");
        }

        for (Reason value : Reason.values()) {
            assertTrue(value.name().matches("^[A-Z_]+$"), "Reason " + value + " should be uppercase with underscores");
        }
    }

    @Test
    void enums_shouldBeSerializable() {
        // Enums are serializable by default, but let's verify some basic properties
        for (ErrorCode value : ErrorCode.values()) {
            assertEquals(value.ordinal(), ErrorCode.valueOf(value.name()).ordinal());
        }

        for (FlagValueType value : FlagValueType.values()) {
            assertEquals(value.ordinal(), FlagValueType.valueOf(value.name()).ordinal());
        }

        for (ProviderEvent value : ProviderEvent.values()) {
            assertEquals(value.ordinal(), ProviderEvent.valueOf(value.name()).ordinal());
        }

        for (ProviderState value : ProviderState.values()) {
            assertEquals(value.ordinal(), ProviderState.valueOf(value.name()).ordinal());
        }

        for (Reason value : Reason.values()) {
            assertEquals(value.ordinal(), Reason.valueOf(value.name()).ordinal());
        }
    }

    @Test
    void providerStateAndEvent_shouldHaveLogicalRelationship() {
        // There should be corresponding states and events for key scenarios
        assertTrue(Arrays.asList(ProviderState.values()).contains(ProviderState.READY));
        assertTrue(Arrays.asList(ProviderEvent.values()).contains(ProviderEvent.PROVIDER_READY));

        assertTrue(Arrays.asList(ProviderState.values()).contains(ProviderState.ERROR));
        assertTrue(Arrays.asList(ProviderEvent.values()).contains(ProviderEvent.PROVIDER_ERROR));

        assertTrue(Arrays.asList(ProviderState.values()).contains(ProviderState.STALE));
        assertTrue(Arrays.asList(ProviderEvent.values()).contains(ProviderEvent.PROVIDER_STALE));
    }

    @Test
    void errorCodeAndReason_shouldHaveLogicalRelationship() {
        // Both should have ERROR variants
        assertTrue(Arrays.asList(ErrorCode.values()).contains(ErrorCode.GENERAL));
        assertTrue(Arrays.asList(Reason.values()).contains(Reason.ERROR));
    }
}
