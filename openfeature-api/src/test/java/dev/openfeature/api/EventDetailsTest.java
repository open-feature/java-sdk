package dev.openfeature.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventDetailsTest {

    @Test
    void builder_shouldCreateEventDetailsWithRequiredFields() {
        ProviderEventDetails providerDetails =
                ProviderEventDetails.builder().message("test message").build();

        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .providerEventDetails(providerDetails)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertNull(eventDetails.getDomain());
        assertNotNull(eventDetails.getProviderEventDetails());
        assertEquals("test message", eventDetails.getMessage());
    }

    @Test
    void builder_shouldCreateEventDetailsWithDomain() {
        ProviderEventDetails providerDetails =
                ProviderEventDetails.builder().message("test message").build();

        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .domain("test-domain")
                .providerEventDetails(providerDetails)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertEquals("test-domain", eventDetails.getDomain());
        assertNotNull(eventDetails.getProviderEventDetails());
    }

    @Test
    void builder_shouldThrowWhenProviderNameIsNull() {
        ProviderEventDetails providerDetails =
                ProviderEventDetails.builder().message("test message").build();

        assertThrows(NullPointerException.class, () -> {
            EventDetails.builder()
                    .providerName(null)
                    .providerEventDetails(providerDetails)
                    .build();
        });
    }

    @Test
    void builder_shouldAllowExplicitNullProviderEventDetails() {
        // The builder creates a default ProviderEventDetails when null, so this should not throw
        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .providerEventDetails(null)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertNotNull(eventDetails.getProviderEventDetails());
    }

    @Test
    void builder_shouldCreateDefaultProviderEventDetailsWhenNotSet() {
        EventDetails eventDetails =
                EventDetails.builder().providerName("test-provider").build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertNotNull(eventDetails.getProviderEventDetails());
        assertNull(eventDetails.getMessage());
        assertNull(eventDetails.getFlagsChanged()); // Default builder creates null flagsChanged
    }

    @Test
    void builder_shouldSupportConvenienceMethodsForFlagsChanged() {
        List<String> flags = Arrays.asList("flag1", "flag2");

        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .flagsChanged(flags)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertEquals(flags, eventDetails.getFlagsChanged());
    }

    @Test
    void builder_shouldSupportConvenienceMethodsForMessage() {
        String message = "Configuration updated";

        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .message(message)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertEquals(message, eventDetails.getMessage());
    }

    @Test
    void builder_shouldSupportConvenienceMethodsForEventMetadata() {
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("version", "1.0").build();

        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .eventMetadata(metadata)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertEquals(metadata, eventDetails.getEventMetadata());
    }

    @Test
    void builder_shouldSupportConvenienceMethodsForErrorCode() {
        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .errorCode(ErrorCode.GENERAL)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertEquals(ErrorCode.GENERAL, eventDetails.getErrorCode());
    }

    @Test
    void builder_shouldCombineConvenienceMethods() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Configuration updated";
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("version", "1.0").build();

        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .domain("test-domain")
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertEquals("test-domain", eventDetails.getDomain());
        assertEquals(flags, eventDetails.getFlagsChanged());
        assertEquals(message, eventDetails.getMessage());
        assertEquals(metadata, eventDetails.getEventMetadata());
        assertEquals(ErrorCode.GENERAL, eventDetails.getErrorCode());
    }

    @Test
    void toBuilder_shouldCreateBuilderWithCurrentState() {
        ProviderEventDetails providerDetails = ProviderEventDetails.builder()
                .message("original message")
                .flagsChanged(Arrays.asList("flag1"))
                .build();

        EventDetails original = EventDetails.builder()
                .providerName("test-provider")
                .domain("test-domain")
                .providerEventDetails(providerDetails)
                .build();

        EventDetails modified = original.toBuilder().message("modified message").build();

        // Original should be unchanged
        assertEquals("original message", original.getMessage());
        assertEquals(Arrays.asList("flag1"), original.getFlagsChanged());

        // Modified should have new message but preserve other fields
        assertEquals("test-provider", modified.getProviderName());
        assertEquals("test-domain", modified.getDomain());
        assertEquals("modified message", modified.getMessage());
        assertEquals(Arrays.asList("flag1"), modified.getFlagsChanged());
    }

    @Test
    void delegation_shouldWorkCorrectly() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Test message";
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();

        ProviderEventDetails providerDetails = ProviderEventDetails.builder()
                .flagsChanged(flags)
                .message(message)
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .providerEventDetails(providerDetails)
                .build();

        // Test delegation to provider event details
        assertEquals(flags, eventDetails.getFlagsChanged());
        assertEquals(message, eventDetails.getMessage());
        assertEquals(metadata, eventDetails.getEventMetadata());
        assertEquals(ErrorCode.GENERAL, eventDetails.getErrorCode());

        // Test direct access
        assertSame(providerDetails, eventDetails.getProviderEventDetails());
    }

    @Test
    void equals_shouldWorkCorrectly() {
        ProviderEventDetails providerDetails =
                ProviderEventDetails.builder().message("test message").build();

        EventDetails event1 = EventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        EventDetails event2 = EventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        EventDetails event3 = EventDetails.builder()
                .providerName("different")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        // Same content should be equal
        assertEquals(event1, event2);
        assertEquals(event2, event1);

        // Different provider name should not be equal
        assertNotEquals(event1, event3);

        // Self-equality
        assertEquals(event1, event1);

        // Null comparison
        assertNotEquals(event1, null);

        // Different class comparison
        assertNotEquals(event1, "not an event");
    }

    @Test
    void hashCode_shouldBeConsistent() {
        ProviderEventDetails providerDetails =
                ProviderEventDetails.builder().message("test message").build();

        EventDetails event1 = EventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        EventDetails event2 = EventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void toString_shouldIncludeAllFields() {
        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .domain("test-domain")
                .message("test message")
                .build();

        String toString = eventDetails.toString();
        assertTrue(toString.contains("test-provider"));
        assertTrue(toString.contains("test-domain"));
        assertTrue(toString.contains("EventDetails"));
    }

    @Test
    void builder_shouldHandleNullDomain() {
        EventDetails eventDetails = EventDetails.builder()
                .providerName("test-provider")
                .domain(null)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertNull(eventDetails.getDomain());
    }
}
