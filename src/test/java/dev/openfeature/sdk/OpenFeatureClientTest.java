package dev.openfeature.sdk;

import java.util.*;

import dev.openfeature.sdk.fixtures.HookFixtures;
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
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        when(api.getProvider()).thenReturn(new DoSomethingProvider());
        when(api.getHooks()).thenReturn(Arrays.asList(mockBooleanHook(), mockStringHook()));

        OpenFeatureClient client = new OpenFeatureClient(api, "name", "version");

        FlagEvaluationDetails<Boolean> actual = client.getBooleanDetails("feature key", Boolean.FALSE);

        assertThat(actual.getValue()).isTrue();
        assertThat(TEST_LOGGER.getLoggingEvents()).filteredOn(event -> event.getLevel().equals(Level.ERROR)).isEmpty();
    }
}
