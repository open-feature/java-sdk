package dev.openfeature.sdk;

import dev.openfeature.sdk.fixtures.HookFixtures;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpenFeatureClientTest implements HookFixtures {

    private Logger logger;

    @BeforeEach
    void set_logger() {
        logger = Mockito.mock(Logger.class);
        LoggerMock.setMock(OpenFeatureClient.class, logger);
    }

    @AfterEach
    void reset_logs() {
        LoggerMock.setMock(OpenFeatureClient.class, logger);
    }

    @Test
    @DisplayName("should not throw exception if hook has different type argument than hookContext")
    void shouldNotThrowExceptionIfHookHasDifferentTypeArgumentThanHookContext() {
        DoSomethingProvider provider = new DoSomethingProvider();
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        when(api.getProvider(any())).thenReturn(provider);
        when(api.getHooks()).thenReturn(Arrays.asList(mockBooleanHook(), mockStringHook()));

        MockProviderRepository mockProviderRepository = new MockProviderRepository(provider, true);
        OpenFeatureClient client = new OpenFeatureClient(mockProviderRepository, api, "name", "version");

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

        MockProviderRepository mockProviderRepository = new MockProviderRepository(mockProvider, true);
        OpenFeatureClient client = new OpenFeatureClient(mockProviderRepository, api, "name", "version");
        client.setEvaluationContext(ctx);

        FlagEvaluationDetails<Boolean> result = client.getBooleanDetails(flag, defaultValue);

        assertThat(result.getValue()).isTrue();
    }

    @Test
    @DisplayName("addHooks should allow chaining by returning the same client instance")
    void addHooksShouldAllowChaining() {
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        OpenFeatureClient client = new OpenFeatureClient(() -> null, api, "name", "version");
        Hook<?> hook1 = Mockito.mock(Hook.class);
        Hook<?> hook2 = Mockito.mock(Hook.class);

        OpenFeatureClient result = client.addHooks(hook1, hook2);
        assertEquals(client, result);
    }

    @Test
    @DisplayName("setEvaluationContext should allow chaining by returning the same client instance")
    void setEvaluationContextShouldAllowChaining() {
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        OpenFeatureClient client = new OpenFeatureClient(() -> null, api, "name", "version");
        EvaluationContext ctx = new ImmutableContext("targeting key", new HashMap<>());

        OpenFeatureClient result = client.setEvaluationContext(ctx);
        assertEquals(client, result);
    }

    @Test
    @DisplayName("Should not call evaluation methods when the provider has state FATAL")
    void shouldNotCallEvaluationMethodsWhenProviderIsInFatalErrorState() {
        MockProvider mockProvider = new MockProvider(ProviderState.FATAL);
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        MockProviderRepository mockProviderRepository = new MockProviderRepository(mockProvider, true);
        OpenFeatureClient client = new OpenFeatureClient(mockProviderRepository, api, "name", "version");
        mockProviderRepository.featureProviderStateManager.onEmit(
                ProviderEvent.PROVIDER_ERROR,
                ProviderEventDetails.builder().errorCode(ErrorCode.PROVIDER_FATAL).build()
        );
        FlagEvaluationDetails<Boolean> details = client.getBooleanDetails("key", true);

        assertThat(mockProvider.isEvaluationCalled()).isFalse();
        assertThat(details.getErrorCode()).isEqualTo(ErrorCode.PROVIDER_FATAL);
    }

    @Test
    @DisplayName("Should not call evaluation methods when the provider has state NOT_READY")
    void shouldNotCallEvaluationMethodsWhenProviderIsInNotReadyState() {
        MockProvider mockProvider = new MockProvider(ProviderState.NOT_READY);
        OpenFeatureAPI api = mock(OpenFeatureAPI.class);
        OpenFeatureClient client = new OpenFeatureClient(new MockProviderRepository(mockProvider, false), api, "name", "version");
        FlagEvaluationDetails<Boolean> details = client.getBooleanDetails("key", true);

        assertThat(mockProvider.isEvaluationCalled()).isFalse();
        assertThat(details.getErrorCode()).isEqualTo(ErrorCode.PROVIDER_NOT_READY);
    }

    private static class MockProviderRepository implements ProviderAccessor {
        private final FeatureProviderStateManager featureProviderStateManager;

        @SneakyThrows
        public MockProviderRepository(FeatureProvider featureProvider, boolean init) {
            this.featureProviderStateManager = new FeatureProviderStateManager(featureProvider);
            if (init) {
                this.featureProviderStateManager.initialize(null);
            }
        }

        @Override
        public FeatureProviderStateManager getProviderStateManager() {
            return featureProviderStateManager;
        }
    }

    private static class MockProvider implements FeatureProvider {
        private final AtomicBoolean evaluationCalled = new AtomicBoolean();
        private final ProviderState providerState;

        public MockProvider(ProviderState providerState) {
            this.providerState = providerState;
        }

        public boolean isEvaluationCalled() {
            return evaluationCalled.get();
        }

        @Override
        public ProviderState getState() {
            return providerState;
        }

        @Override
        public Metadata getMetadata() {
            return null;
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
            evaluationCalled.set(true);
            return null;
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            evaluationCalled.set(true);
            return null;
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
            evaluationCalled.set(true);
            return null;
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            evaluationCalled.set(true);
            return null;
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            evaluationCalled.set(true);
            return null;
        }
    }
}
