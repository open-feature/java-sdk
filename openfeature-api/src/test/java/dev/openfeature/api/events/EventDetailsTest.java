package dev.openfeature.api.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.types.Metadata;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventDetailsTest {

    @Test
    void builder_shouldCreateEventDetailsWithRequiredFields() {
        ProviderEventDetails providerDetails = ProviderEventDetails.of("test message");

        DefaultEventDetails eventDetails = DefaultEventDetails.builder()
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
        ProviderEventDetails providerDetails = ProviderEventDetails.of("test message");

        DefaultEventDetails eventDetails = DefaultEventDetails.builder()
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
        ProviderEventDetails providerDetails = ProviderEventDetails.of("test message");

        assertThrows(NullPointerException.class, () -> {
            DefaultEventDetails.builder()
                    .providerName(null)
                    .providerEventDetails(providerDetails)
                    .build();
        });
    }

    @Test
    void builder_shouldAllowExplicitNullProviderEventDetails() {
        // The builder creates a default ProviderEventDetails when null, so this should not throw
        DefaultEventDetails eventDetails = DefaultEventDetails.builder()
                .providerName("test-provider")
                .providerEventDetails(null)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertNotNull(eventDetails.getProviderEventDetails());
    }

    @Test
    void builder_shouldCreateDefaultProviderEventDetailsWhenNotSet() {
        DefaultEventDetails eventDetails =
                DefaultEventDetails.builder().providerName("test-provider").build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertNotNull(eventDetails.getProviderEventDetails());
        assertNull(eventDetails.getMessage());
        assertNull(eventDetails.getFlagsChanged()); // Default builder creates null flagsChanged
    }

    @Test
    void delegation_shouldWorkCorrectly() {
        List<String> flags = Arrays.asList("flag1", "flag2");
        String message = "Test message";
        var metadata = Metadata.immutableBuilder().add("key", "value").build();

        ProviderEventDetails providerDetails = ProviderEventDetails.of(message, flags, metadata, ErrorCode.GENERAL);

        DefaultEventDetails eventDetails = DefaultEventDetails.builder()
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
        ProviderEventDetails providerDetails = ProviderEventDetails.of("test message");

        EventDetails event1 = DefaultEventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        EventDetails event2 = DefaultEventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        EventDetails event3 = DefaultEventDetails.builder()
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
        ProviderEventDetails providerDetails = ProviderEventDetails.of("test message");

        EventDetails event1 = DefaultEventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        EventDetails event2 = DefaultEventDetails.builder()
                .providerName("provider")
                .domain("domain")
                .providerEventDetails(providerDetails)
                .build();

        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void toString_shouldIncludeAllFields() {
        EventDetails eventDetails = DefaultEventDetails.builder()
                .providerName("test-provider")
                .domain("test-domain")
                .providerEventDetails(ProviderEventDetails.of("test message"))
                .build();

        String toString = eventDetails.toString();
        assertTrue(toString.contains("test-provider"));
        assertTrue(toString.contains("test-domain"));
        assertTrue(toString.contains("EventDetails"));
    }

    @Test
    void builder_shouldHandleNullDomain() {
        EventDetails eventDetails = DefaultEventDetails.builder()
                .providerName("test-provider")
                .domain(null)
                .build();

        assertEquals("test-provider", eventDetails.getProviderName());
        assertNull(eventDetails.getDomain());
    }
}
