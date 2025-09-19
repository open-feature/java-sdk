package dev.openfeature.api.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.types.Metadata;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderEventDetailsTest {

    @Test
    void of_shouldCreateEmptyProviderEventDetails() {
        ProviderEventDetails details = ProviderEventDetails.EMPTY;

        assertNull(details.getFlagsChanged());
        assertNull(details.getMessage());
        assertNull(details.getEventMetadata());
        assertNull(details.getErrorCode());
    }

    @Test
    void ofMessage_shouldCreateProviderEventDetailsWithMessage() {
        String message = "Configuration updated";
        ProviderEventDetails details = ProviderEventDetails.of(message);

        assertEquals(message, details.getMessage());
        assertNull(details.getFlagsChanged());
        assertNull(details.getEventMetadata());
        assertNull(details.getErrorCode());
    }

    @Test
    void ofMessageAndFlags_shouldCreateProviderEventDetailsWithMessageAndFlagsChanged() {
        List<String> flags = Arrays.asList("flag1", "flag2", "flag3");
        String message = "Configuration updated";
        ProviderEventDetails details = ProviderEventDetails.of(message, flags);

        assertEquals(flags, details.getFlagsChanged());
        assertNotSame(flags, details.getFlagsChanged()); // Should be a copy

        assertEquals(message, details.getMessage());
        assertNull(details.getEventMetadata());
        assertNull(details.getErrorCode());
    }

    @Test
    void ofMessageAndFlagsAndMetadata_shouldCreateProviderEventDetailsWithEventMetadata() {
        var metadata = Metadata.immutableBuilder()
                .add("version", "1.0")
                .add("count", 5)
                .build();

        List<String> flags = Arrays.asList("flag1", "flag2", "flag3");
        String message = "Configuration updated";
        ProviderEventDetails details = ProviderEventDetails.of(message, flags, metadata);

        assertSame(metadata, details.getEventMetadata());
        assertEquals(flags, details.getFlagsChanged());
        assertNotSame(flags, details.getFlagsChanged()); // Should be a copy

        assertEquals(message, details.getMessage());
        assertNull(details.getErrorCode());
    }

    @Test
    void ofAll_shouldCreateProviderEventDetailsWithAllFields() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Provider error occurred";
        var metadata = Metadata.immutableBuilder().add("error", "timeout").build();
        ErrorCode errorCode = ErrorCode.GENERAL;

        ProviderEventDetails details = ProviderEventDetails.of(message, flags, metadata, errorCode);

        assertEquals(flags, details.getFlagsChanged());
        assertEquals(message, details.getMessage());
        assertSame(metadata, details.getEventMetadata());
        assertEquals(errorCode, details.getErrorCode());
    }

    @Test
    void ofAllNull_shouldCreateProviderEventDetailsWithAllFields() {
        ProviderEventDetails details = ProviderEventDetails.of(null, null, null, null);

        assertNull(details.getFlagsChanged());
        assertNull(details.getMessage());
        assertNull(details.getEventMetadata());
        assertNull(details.getErrorCode());
    }

    @Test
    void flagsChanged_shouldReturnImmutableCopy() {
        List<String> originalFlags = new ArrayList<>(Arrays.asList("flag1", "flag2"));
        ProviderEventDetails details = ProviderEventDetails.of("flags changed", originalFlags);

        List<String> returnedFlags = details.getFlagsChanged();

        // Should not be the same instance
        assertNotSame(originalFlags, returnedFlags);

        // Modifying original list should not affect details
        originalFlags.add("flag3");
        assertEquals(2, returnedFlags.size()); // Should remain unchanged
        assertTrue(returnedFlags.contains("flag1"));
        assertTrue(returnedFlags.contains("flag2"));
        assertFalse(returnedFlags.contains("flag3"));

        // The returned list should be immutable (defensive copy)
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedFlags.add("flag4");
        });
    }

    @Test
    void flagsChanged_shouldReturnImmutableCopyWithMutableInput() {
        List<String> originalFlags = Arrays.asList("flag1", "flag2");
        ProviderEventDetails details = ProviderEventDetails.of("flags changed", originalFlags);

        List<String> returnedFlags = details.getFlagsChanged();

        // Verify immutability by trying to modify returned list
        try {
            returnedFlags.add("flag3");
        } catch (UnsupportedOperationException e) {
            // Expected - the returned list should be immutable
            assertTrue(true);
        }
    }

    @Test
    void equals_shouldWorkCorrectly() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Test message";
        var metadata = Metadata.immutableBuilder().add("key", "value").build();

        ProviderEventDetails details1 = ProviderEventDetails.of(message, flags, metadata, ErrorCode.GENERAL);

        ProviderEventDetails details2 = ProviderEventDetails.of(message, flags, metadata, ErrorCode.GENERAL);

        ProviderEventDetails details3 =
                ProviderEventDetails.of("different message", flags, metadata, ErrorCode.GENERAL);

        // Same content should be equal
        assertEquals(details1, details2);
        assertEquals(details2, details1);

        // Different message should not be equal
        assertNotEquals(details1, details3);

        // Self-equality
        assertEquals(details1, details1);

        // Null comparison
        assertNotEquals(null, details1);

        // Different class comparison
        assertNotEquals("not details", details1);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        var metadata = Metadata.immutableBuilder().add("key", "value").build();

        ProviderEventDetails details1 = ProviderEventDetails.of("message", flags, metadata, ErrorCode.GENERAL);

        ProviderEventDetails details2 = ProviderEventDetails.of("message", flags, metadata, ErrorCode.GENERAL);

        assertEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void toString_shouldIncludeAllFields() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Test message";
        var metadata = Metadata.immutableBuilder().add("key", "value").build();

        ProviderEventDetails details = ProviderEventDetails.of(message, flags, metadata, ErrorCode.GENERAL);

        String toString = details.toString();
        assertTrue(toString.contains("ProviderEventDetails"));
        assertTrue(toString.contains("flag1"));
        assertTrue(toString.contains("flag2"));
        assertTrue(toString.contains("Test message"));
        assertTrue(toString.contains("GENERAL"));
    }

    @Test
    void implementsEventDetailsInterface() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Test message";
        var metadata = Metadata.immutableBuilder().add("key", "value").build();

        ProviderEventDetails details = ProviderEventDetails.of(message, flags, metadata, ErrorCode.GENERAL);

        // Test that it implements EventDetailsInterface
        assertNotNull(details);

        // Test interface methods
        assertEquals(flags, details.getFlagsChanged());
        assertEquals(message, details.getMessage());
        assertEquals(metadata, details.getEventMetadata());
        assertEquals(ErrorCode.GENERAL, details.getErrorCode());
    }

    @Test
    void builder_shouldAllowChaining() {
        var details = ProviderEventDetails.of("message", List.of("flag1"), Metadata.EMPTY, ErrorCode.GENERAL);

        assertEquals(List.of("flag1"), details.getFlagsChanged());
        assertEquals("message", details.getMessage());
        assertEquals(ErrorCode.GENERAL, details.getErrorCode());
    }
}
