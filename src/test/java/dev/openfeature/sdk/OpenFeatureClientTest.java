package dev.openfeature.sdk;

import java.util.*;

import dev.openfeature.sdk.fixtures.HookFixtures;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OpenFeatureClientTest implements HookFixtures {

    private Logger logger;

    @BeforeEach void set_logger() {
        logger = Mockito.mock(Logger.class);
        LoggerMock.setMock(OpenFeatureClient.class, logger);
    }

    @AfterEach void reset_logs() {
        LoggerMock.setMock(OpenFeatureClient.class, logger);
    }
    @Test
    @DisplayName("should not throw exception if hook has different type argument than hookContext")
    void shouldNotThrowExceptionIfHookHasDifferentTypeArgumentThanHookContext() {
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        when(api.getProvider(any())).thenReturn(new DoSomethingProvider());
        when(api.getHooks()).thenReturn(Arrays.asList(mockBooleanHook(), mockStringHook()));

        OpenFeatureClient client = new OpenFeatureClient(api, "name", "version");

        FlagEvaluationDetails<Boolean> actual = client.getBooleanDetails("feature key", Boolean.FALSE);

        assertThat(actual.getValue()).isTrue();
        // I dislike this, but given the mocking tools available, there's no way that I know of to say "no errors were logged"
        Mockito.verify(logger, never()).error(any());
        Mockito.verify(logger, never()).error(anyString(), any(Throwable.class));
        Mockito.verify(logger, never()).error(anyString(), any(Object.class));
        Mockito.verify(logger, never()).error(anyString(), any(), any());
        Mockito.verify(logger, never()).error(anyString(), any(), any());
    }

    @Test
    void mergeContextTest() {
        String flag = "feature key";
        boolean defaultValue = false;
        String targetingKey = "targeting key";
        EvaluationContext ctx = new ImmutableContext(targetingKey, new HashMap<>());
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        FeatureProvider mockProvider = mock(FeatureProvider.class);
        // this makes it so that true is returned only if the targeting key set at the client level is honored
        when(mockProvider.getBooleanEvaluation(
          eq(flag), eq(defaultValue), argThat(
            context -> context.getTargetingKey().equals(targetingKey)))).thenReturn(ProviderEvaluation.<Boolean>builder()
          .value(true).build());
        when(api.getProvider()).thenReturn(mockProvider);
        when(api.getProvider(any())).thenReturn(mockProvider);


        OpenFeatureClient client = new OpenFeatureClient(api, "name", "version");
        client.setEvaluationContext(ctx);

        FlagEvaluationDetails<Boolean> result = client.getBooleanDetails(flag, defaultValue);

        assertThat(result.getValue()).isTrue();
    }

    @Test
    @DisplayName("addHooks should allow chaining by returning the same client instance")
    void addHooksShouldAllowChaining() {
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        OpenFeatureClient client = new OpenFeatureClient(api, "name", "version");
        Hook<?> hook1 = Mockito.mock(Hook.class);
        Hook<?> hook2 = Mockito.mock(Hook.class);

        OpenFeatureClient result = client.addHooks(hook1, hook2);
        assertEquals(client, result);  
    }

    @Test
    @DisplayName("setEvaluationContext should allow chaining by returning the same client instance")
    void setEvaluationContextShouldAllowChaining() {
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        OpenFeatureClient client = new OpenFeatureClient(api, "name", "version");
        EvaluationContext ctx = new ImmutableContext("targeting key", new HashMap<>());

        OpenFeatureClient result = client.setEvaluationContext(ctx);
        assertEquals(client, result);
    }
   
}
