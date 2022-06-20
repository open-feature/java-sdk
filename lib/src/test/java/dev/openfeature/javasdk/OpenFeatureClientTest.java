package dev.openfeature.javasdk;

import java.util.*;

import dev.openfeature.javasdk.fixtures.HookFixtures;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OpenFeatureClientTest implements HookFixtures {

    private static final TestLogger TEST_LOGGER = TestLoggerFactory.getTestLogger(OpenFeatureClient.class);

    @Test
    @DisplayName("should not throw exception if hook has different type argument than hookContext")
    void shouldNotThrowExceptionIfHookHasDifferentTypeArgumentThanHookContext() {
        TEST_LOGGER.clear();
        var api = mock(OpenFeatureAPI.class);
        when(api.getProvider()).thenReturn(new DoSomethingProvider());
        when(api.getApiHooks()).thenReturn(List.of(mockBooleanHook(), mockStringHook()));

        var client = new OpenFeatureClient(api, "name", "version");

        var actual = client.getBooleanDetails("feature key", Boolean.FALSE);

        assertThat(actual.getValue()).isTrue();
        assertThat(TEST_LOGGER.getLoggingEvents()).filteredOn(event -> event.getLevel().equals(Level.ERROR)).isEmpty();
    }
}
