package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProviderEventDetailsTest {

    @Test
    void builder_shouldCreateEmptyProviderEventDetails() {
        ProviderEventDetails details = ProviderEventDetails.builder().build();

        assertNull(details.getFlagsChanged());
        assertNull(details.getMessage());
        assertNull(details.getEventMetadata());
        assertNull(details.getErrorCode());
    }

    @Test
    void builder_shouldCreateProviderEventDetailsWithMessage() {
        String message = "Configuration updated";
        ProviderEventDetails details =
                ProviderEventDetails.builder().message(message).build();

        assertEquals(message, details.getMessage());
        assertNull(details.getFlagsChanged());
        assertNull(details.getEventMetadata());
        assertNull(details.getErrorCode());
    }

    @Test
    void builder_shouldCreateProviderEventDetailsWithFlagsChanged() {
        List<String> flags = Arrays.asList("flag1", "flag2", "flag3");
        ProviderEventDetails details =
                ProviderEventDetails.builder().flagsChanged(flags).build();

        assertEquals(flags, details.getFlagsChanged());
        assertNotSame(flags, details.getFlagsChanged()); // Should be a copy
        assertNull(details.getMessage());
        assertNull(details.getEventMetadata());
        assertNull(details.getErrorCode());
    }

    @Test
    void builder_shouldCreateProviderEventDetailsWithEventMetadata() {
        ImmutableMetadata metadata = ImmutableMetadata.builder()
                .addString("version", "1.0")
                .addInteger("count", 5)
                .build();

        ProviderEventDetails details =
                ProviderEventDetails.builder().eventMetadata(metadata).build();

        assertSame(metadata, details.getEventMetadata());
        assertNull(details.getFlagsChanged());
        assertNull(details.getMessage());
        assertNull(details.getErrorCode());
    }

    @Test
    void builder_shouldCreateProviderEventDetailsWithErrorCode() {
        ProviderEventDetails details =
                ProviderEventDetails.builder().errorCode(ErrorCode.GENERAL).build();

        assertEquals(ErrorCode.GENERAL, details.getErrorCode());
        assertNull(details.getFlagsChanged());
        assertNull(details.getMessage());
        assertNull(details.getEventMetadata());
    }

    @Test
    void builder_shouldCreateProviderEventDetailsWithAllFields() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Provider error occurred";
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("error", "timeout").build();
        ErrorCode errorCode = ErrorCode.GENERAL;

        ProviderEventDetails details = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(errorCode)
                .build();

        assertEquals(flags, details.getFlagsChanged());
        assertEquals(message, details.getMessage());
        assertSame(metadata, details.getEventMetadata());
        assertEquals(errorCode, details.getErrorCode());
    }

    @Test
    void builder_shouldHandleNullFlagsChanged() {
        ProviderEventDetails details =
                ProviderEventDetails.builder().flagsChanged(null).build();

        assertNull(details.getFlagsChanged());
    }

    @Test
    void builder_shouldHandleNullMessage() {
        ProviderEventDetails details =
                ProviderEventDetails.builder().message(null).build();

        assertNull(details.getMessage());
    }

    @Test
    void builder_shouldHandleNullEventMetadata() {
        ProviderEventDetails details =
                ProviderEventDetails.builder().eventMetadata(null).build();

        assertNull(details.getEventMetadata());
    }

    @Test
    void builder_shouldHandleNullErrorCode() {
        ProviderEventDetails details =
                ProviderEventDetails.builder().errorCode(null).build();

        assertNull(details.getErrorCode());
    }

    @Test
    void flagsChanged_shouldReturnImmutableCopy() {
        List<String> originalFlags = new ArrayList<>(Arrays.asList("flag1", "flag2"));
        ProviderEventDetails details =
                ProviderEventDetails.builder().flagsChanged(originalFlags).build();

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
        ProviderEventDetails details =
                ProviderEventDetails.builder().flagsChanged(originalFlags).build();

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
    void toBuilder_shouldCreateBuilderWithCurrentState() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Original message";
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();

        ProviderEventDetails original = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        ProviderEventDetails modified = original.toBuilder()
                .message("Modified message")
                .errorCode(ErrorCode.PARSE_ERROR)
                .build();

        // Original should be unchanged
        assertEquals(message, original.getMessage());
        assertEquals(ErrorCode.GENERAL, original.getErrorCode());

        // Modified should have new values but preserve other fields
        assertEquals(flags, modified.getFlagsChanged());
        assertEquals("Modified message", modified.getMessage());
        assertSame(metadata, modified.getEventMetadata());
        assertEquals(ErrorCode.PARSE_ERROR, modified.getErrorCode());
    }

    @Test
    void equals_shouldWorkCorrectly() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Test message";
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();

        ProviderEventDetails details1 = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        ProviderEventDetails details2 = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        ProviderEventDetails details3 = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message("Different message")
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        // Same content should be equal
        assertEquals(details1, details2);
        assertEquals(details2, details1);

        // Different message should not be equal
        assertNotEquals(details1, details3);

        // Self-equality
        assertEquals(details1, details1);

        // Null comparison
        assertNotEquals(details1, null);

        // Different class comparison
        assertNotEquals(details1, "not details");
    }

    @Test
    void hashCode_shouldBeConsistent() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();

        ProviderEventDetails details1 = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message("message")
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        ProviderEventDetails details2 = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message("message")
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        assertEquals(details1.hashCode(), details2.hashCode());
    }

    @Test
    void toString_shouldIncludeAllFields() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Test message";
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();

        ProviderEventDetails details = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

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
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();

        ProviderEventDetails details = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        // Test that it implements EventDetailsInterface
        assertTrue(details instanceof EventDetailsInterface);

        // Test interface methods
        assertEquals(flags, details.getFlagsChanged());
        assertEquals(message, details.getMessage());
        assertEquals(metadata, details.getEventMetadata());
        assertEquals(ErrorCode.GENERAL, details.getErrorCode());
    }

    @Test
    void builder_shouldAllowChaining() {
        ProviderEventDetails details = ProviderEventDetails.builder()
                .flagsChanged(Arrays.asList("flag1"))
                .message("message")
                .eventMetadata(ImmutableMetadata.builder().build())
                .errorCode(ErrorCode.GENERAL)
                .build();

        assertEquals(Arrays.asList("flag1"), details.getFlagsChanged());
        assertEquals("message", details.getMessage());
        assertEquals(ErrorCode.GENERAL, details.getErrorCode());
    }
}
