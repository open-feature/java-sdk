package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import dev.openfeature.sdk.exceptions.FatalError;
import dev.openfeature.sdk.fixtures.HookFixtures;
import dev.openfeature.sdk.testutils.testProvider.TestProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;

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
        OpenFeatureAPI api = new OpenFeatureAPI();
        api.setProviderAndWait(
                "shouldNotThrowExceptionIfHookHasDifferentTypeArgumentThanHookContext",
                TestProvider.builder().initsToReady());
        Client client = api.getClient("shouldNotThrowExceptionIfHookHasDifferentTypeArgumentThanHookContext");
        client.addHooks(mockBooleanHook(), mockStringHook());
        FlagEvaluationDetails<Boolean> actual = client.getBooleanDetails("feature key", Boolean.TRUE);

        assertThat(actual.getValue()).isTrue();
        // I dislike this, but given the mocking tools available, there's no way that I know of to say "no errors were
        // logged"
        Mockito.verify(logger, never()).error(any());
        Mockito.verify(logger, never()).error(anyString(), any(Throwable.class));
        Mockito.verify(logger, never()).error(anyString(), any(Object.class));
        Mockito.verify(logger, never()).error(anyString(), any(), any());
        Mockito.verify(logger, never()).error(anyString(), any(), any());
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

    @Test
    @DisplayName("Should not call evaluation methods when the provider has state FATAL")
    void shouldNotCallEvaluationMethodsWhenProviderIsInFatalErrorState() {
        var provider = TestProvider.builder().initWaitsFor(100).initsToFatal();
        OpenFeatureAPI api = new OpenFeatureAPI();
        Client client = api.getClient("shouldNotCallEvaluationMethodsWhenProviderIsInFatalErrorState");

        assertThrows(
                FatalError.class,
                () -> api.setProviderAndWait(
                        "shouldNotCallEvaluationMethodsWhenProviderIsInFatalErrorState", provider));
        FlagEvaluationDetails<Boolean> details = client.getBooleanDetails("key", true);
        assertThat(details.getErrorCode()).isEqualTo(ErrorCode.PROVIDER_FATAL);
    }

    @Test
    @DisplayName("Should not call evaluation methods when the provider has state NOT_READY")
    void shouldNotCallEvaluationMethodsWhenProviderIsInNotReadyState() {
        var awaitable = new Awaitable();
        var provider = TestProvider.builder().initWaitsFor(awaitable).initsToReady();
        OpenFeatureAPI api = new OpenFeatureAPI();
        api.setProvider("shouldNotCallEvaluationMethodsWhenProviderIsInNotReadyState", provider);
        Client client = api.getClient("shouldNotCallEvaluationMethodsWhenProviderIsInNotReadyState");
        FlagEvaluationDetails<Boolean> details = client.getBooleanDetails("key", true);

        assertThat(details.getErrorCode()).isEqualTo(ErrorCode.PROVIDER_NOT_READY);
        awaitable.wakeup();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("Should support usage of HookData with/without error")
    void shouldSupportUsageOfHookData(boolean isError) {
        OpenFeatureAPI api = new OpenFeatureAPI();
        api.setProviderAndWait(
                "shouldSupportUsageOfHookData",
                TestProvider.builder().allowUnknownFlags(!isError).initsToReady());

        var testHook = new TestHookWithData("test-data");
        api.addHooks(testHook);

        Client client = api.getClient("shouldSupportUsageOfHookData");
        client.getBooleanDetails("key", true);

        assertThat(testHook.hookData.get("before")).isEqualTo("test-data");
        assertThat(testHook.hookData.get("finallyAfter")).isEqualTo("test-data");
        if (isError) {
            assertThat(testHook.hookData.get("after")).isEqualTo(null);
            assertThat(testHook.hookData.get("error")).isEqualTo("test-data");
        } else {
            assertThat(testHook.hookData.get("after")).isEqualTo("test-data");
            assertThat(testHook.hookData.get("error")).isEqualTo(null);
        }
    }

    @Test
    void flagEvaluationsUseTheCorrectContext() {
        OpenFeatureAPI api = new OpenFeatureAPI();
        api.setTransactionContextPropagator(new ThreadLocalTransactionContextPropagator());
        var provider = TestProvider.builder().allowUnknownFlags(true).initsToReady();
        api.setProviderAndWait(provider);

        var apiContext = new MutableContext("api-level", Map.of("api", new Value("api"), "override", new Value("api")));
        var transactionContext = new MutableContext(
                "transaction-level",
                Map.of("transaction", new Value("transaction"), "override", new Value("transaction")));
        var clientContext = new MutableContext(
                "client-level", Map.of("client", new Value("client"), "override", new Value("client")));
        var invocationContext = new MutableContext(
                "invocation-level", Map.of("invocation", new Value("invocation"), "override", new Value("invocation")));

        var hookContext =
                new MutableContext("hook-level", Map.of("hook", new Value("hook"), "override", new Value("hook")));
        var ctxHook = new Hook<>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Object> ctx, Map<String, Object> hints) {
                return Optional.of(hookContext);
            }
        };

        api.setEvaluationContext(apiContext);
        api.setTransactionContext(transactionContext);

        var client = api.getClient();
        client.addHooks(ctxHook);
        client.setEvaluationContext(clientContext);

        client.getStringValue("flag", "idc", invocationContext);

        var flagEvaluations = provider.getFlagEvaluations();
        assertThat(flagEvaluations).hasSize(1);

        var evaluation = flagEvaluations.get(0);
        assertThat(evaluation.evaluationContext.getValue("api").asString()).isEqualTo("api");
        assertThat(evaluation.evaluationContext.getValue("transaction").asString())
                .isEqualTo("transaction");
        assertThat(evaluation.evaluationContext.getValue("client").asString()).isEqualTo("client");
        assertThat(evaluation.evaluationContext.getValue("invocation").asString())
                .isEqualTo("invocation");
        assertThat(evaluation.evaluationContext.getValue("hook").asString()).isEqualTo("hook");
        assertThat(evaluation.evaluationContext.getValue("override").asString()).isEqualTo("hook");
        assertThat(evaluation.evaluationContext.getTargetingKey()).isEqualTo("hook-level");
    }
}
