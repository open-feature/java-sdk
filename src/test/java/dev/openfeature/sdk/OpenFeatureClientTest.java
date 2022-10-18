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

    @Test
    void mergeContextTest() {
        TEST_LOGGER.clear();

        String flag = "feature key";
        boolean defaultValue = false;
        String targetingKey = "targeting key";
        EvaluationContext ctx = new MutableContext(targetingKey);

        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        FeatureProvider mockProvider = mock(FeatureProvider.class);
        // this makes it so that true is returned only if the targeting key set at the client level is honored
        when(mockProvider.getBooleanEvaluation(
          eq(flag), eq(defaultValue), argThat(
            context -> context.getTargetingKey().equals(targetingKey)))).thenReturn(ProviderEvaluation.<Boolean>builder()
          .value(true).build());
        when(api.getProvider()).thenReturn(mockProvider);

        OpenFeatureClient client = new OpenFeatureClient(api, "name", "version");
        client.setEvaluationContext(ctx);

        FlagEvaluationDetails<Boolean> result = client.getBooleanDetails(flag, defaultValue);

        assertThat(result.getValue()).isTrue();
    }
}
