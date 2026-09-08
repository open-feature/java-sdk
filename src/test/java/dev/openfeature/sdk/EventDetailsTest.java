package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventDetailsTest {

    @Test
    @DisplayName("should carry the error code a provider set, so API-level handlers can see it")
    void shouldCopyErrorCode() {
        ProviderEventDetails providerEventDetails = ProviderEventDetails.builder()
                .errorCode(ErrorCode.PROVIDER_NOT_READY)
                .build();

        EventDetails details = EventDetails.fromProviderEventDetails(providerEventDetails, "provider");

        assertThat(details.getErrorCode()).isEqualTo(ErrorCode.PROVIDER_NOT_READY);
    }

    @Test
    @DisplayName("should leave the error code unset when the provider did not set one")
    void shouldLeaveErrorCodeUnsetWhenAbsent() {
        EventDetails details = EventDetails.fromProviderEventDetails(
                ProviderEventDetails.builder().build(), "provider");

        assertThat(details.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("should carry every field of the provider event details")
    void shouldCopyEveryField() {
        ImmutableMetadata metadata =
                ImmutableMetadata.builder().addString("key", "value").build();
        ProviderEventDetails providerEventDetails = ProviderEventDetails.builder()
                .flagsChanged(Arrays.asList("flag1", "flag2"))
                .message("message")
                .eventMetadata(metadata)
                .errorCode(ErrorCode.GENERAL)
                .build();

        EventDetails details = EventDetails.fromProviderEventDetails(providerEventDetails, "provider", "domain");

        assertThat(details.getFlagsChanged()).isEqualTo(providerEventDetails.getFlagsChanged());
        assertThat(details.getMessage()).isEqualTo(providerEventDetails.getMessage());
        assertThat(details.getEventMetadata()).isEqualTo(providerEventDetails.getEventMetadata());
        assertThat(details.getErrorCode()).isEqualTo(providerEventDetails.getErrorCode());
        assertThat(details.getProviderName()).isEqualTo("provider");
        assertThat(details.getDomain()).isEqualTo("domain");
    }
}
