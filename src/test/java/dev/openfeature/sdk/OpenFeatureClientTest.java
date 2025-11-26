package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import dev.openfeature.sdk.exceptions.FatalError;
import dev.openfeature.sdk.fixtures.HookFixtures;
import dev.openfeature.sdk.testutils.testProvider.TestProvider;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
            assertThat(testHook.hookData.get("after")).isNull();
            assertThat(testHook.hookData.get("error")).isEqualTo("test-data");
        } else {
            assertThat(testHook.hookData.get("after")).isEqualTo("test-data");
            assertThat(testHook.hookData.get("error")).isNull();
        }
    }

    @ParameterizedTest
    @EnumSource(FlagValueType.class)
    @DisplayName("Should call hooks that support the flag value type")
    void shouldExecuteAppropriateHooks(FlagValueType flagValueType) {
        var allTypes = FlagValueType.values();
        var apiHooks = new TypedTestHook[allTypes.length];
        var clientHooks = new TypedTestHook[allTypes.length];
        var providerHooks = new TypedTestHook[allTypes.length];
        var evaluationHooks = new TypedTestHook[allTypes.length];
        for (int i = 0; i < allTypes.length; i++) {
            apiHooks[i] = new TypedTestHook(allTypes[i]);
            clientHooks[i] = new TypedTestHook(allTypes[i]);
            providerHooks[i] = new TypedTestHook(allTypes[i]);
            evaluationHooks[i] = new TypedTestHook(allTypes[i]);
        }
        var allHooks = new TypedTestHook[][] {apiHooks, clientHooks, providerHooks, evaluationHooks};

        OpenFeatureAPI api = new OpenFeatureAPI();
        var provider = TestProvider.builder()
                .withHooks(providerHooks)
                .allowUnknownFlags()
                .initsToReady();
        api.setProviderAndWait(provider);

        Client client = api.getClient();

        api.addHooks(apiHooks);
        client.addHooks(clientHooks);

        var options =
                FlagEvaluationOptions.builder().hooks(List.of(evaluationHooks)).build();

        if (flagValueType == FlagValueType.BOOLEAN) {
            client.getBooleanDetails("key", true, ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.STRING) {
            client.getStringDetails("key", "default", ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.INTEGER) {
            client.getIntegerDetails("key", 42, ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.DOUBLE) {
            client.getDoubleValue("key", 3.14, ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.OBJECT) {
            client.getObjectDetails("key", new Value(1), ImmutableContext.EMPTY, options);
        }

        for (TypedTestHook[] level : allHooks) {
            for (TypedTestHook hook : level) {
                assertEquals(
                        flagValueType == hook.flagValueType,
                        hook.beforeCalled.get(),
                        () -> hook.flagValueType
                                + " hook called? "
                                + hook.beforeCalled.get()
                                + ", should have been called? "
                                + (flagValueType == hook.flagValueType));
                assertEquals(
                        flagValueType == hook.flagValueType,
                        hook.afterCalled.get(),
                        () -> hook.flagValueType
                                + " hook called? "
                                + hook.afterCalled.get()
                                + ", should have been called? "
                                + (flagValueType == hook.flagValueType));
                assertEquals(
                        flagValueType == hook.flagValueType,
                        hook.finallyAfterCalled.get(),
                        () -> hook.flagValueType
                                + " hook called? "
                                + hook.finallyAfterCalled.get()
                                + ", should have been called? "
                                + (flagValueType == hook.flagValueType));
                assertFalse(hook.errorCalled.get());
            }
        }
    }

    @ParameterizedTest
    @EnumSource(FlagValueType.class)
    @DisplayName("Should call hooks that support the flag value type in error scenarios")
    void shouldExecuteAppropriateErrorHooks(FlagValueType flagValueType) {
        var allTypes = FlagValueType.values();
        var apiHooks = new TypedTestHook[allTypes.length];
        var clientHooks = new TypedTestHook[allTypes.length];
        var providerHooks = new TypedTestHook[allTypes.length];
        var evaluationHooks = new TypedTestHook[allTypes.length];
        for (int i = 0; i < allTypes.length; i++) {
            apiHooks[i] = new TypedTestHook(allTypes[i]);
            clientHooks[i] = new TypedTestHook(allTypes[i]);
            providerHooks[i] = new TypedTestHook(allTypes[i]);
            evaluationHooks[i] = new TypedTestHook(allTypes[i]);
        }
        var allHooks = new TypedTestHook[][] {apiHooks, clientHooks, providerHooks, evaluationHooks};

        OpenFeatureAPI api = new OpenFeatureAPI();
        var provider = TestProvider.builder().withHooks(providerHooks).initsToReady();
        api.setProviderAndWait(provider);

        Client client = api.getClient();

        api.addHooks(apiHooks);
        client.addHooks(clientHooks);

        var options =
                FlagEvaluationOptions.builder().hooks(List.of(evaluationHooks)).build();

        if (flagValueType == FlagValueType.BOOLEAN) {
            client.getBooleanDetails("key", true, ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.STRING) {
            client.getStringDetails("key", "default", ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.INTEGER) {
            client.getIntegerDetails("key", 42, ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.DOUBLE) {
            client.getDoubleValue("key", 3.14, ImmutableContext.EMPTY, options);
        } else if (flagValueType == FlagValueType.OBJECT) {
            client.getObjectDetails("key", new Value(1), ImmutableContext.EMPTY, options);
        }

        for (TypedTestHook[] level : allHooks) {
            for (TypedTestHook hook : level) {
                assertEquals(
                        flagValueType == hook.flagValueType,
                        hook.beforeCalled.get(),
                        () -> hook.flagValueType
                                + " hook called? "
                                + hook.beforeCalled.get()
                                + ", should have been called? "
                                + (flagValueType == hook.flagValueType));
                assertEquals(
                        flagValueType == hook.flagValueType,
                        hook.errorCalled.get(),
                        () -> hook.flagValueType
                                + " hook called? "
                                + hook.errorCalled.get()
                                + ", should have been called? "
                                + (flagValueType == hook.flagValueType));
                assertEquals(
                        flagValueType == hook.flagValueType,
                        hook.finallyAfterCalled.get(),
                        () -> hook.flagValueType
                                + " hook called? "
                                + hook.finallyAfterCalled.get()
                                + ", should have been called? "
                                + (flagValueType == hook.flagValueType));
                assertFalse(hook.afterCalled.get());
            }
        }
    }
}
