package dev.openfeature.sdk;

import static dev.openfeature.sdk.DoSomethingProvider.DEFAULT_METADATA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;

import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.fixtures.HookFixtures;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;
import dev.openfeature.sdk.testutils.TestEventsProvider;
import lombok.SneakyThrows;

class FlagEvaluationSpecTest implements HookFixtures {

    private Logger logger;
    private OpenFeatureAPI api;

    private Client _client() {
        FeatureProviderTestUtils.setFeatureProvider(new NoOpProvider());
        return api.getClient();
    }

    @BeforeEach
    void getApiInstance() {
        api = OpenFeatureAPI.getInstance();
    }

    @AfterEach void reset_ctx() {
        api.setEvaluationContext(null);
    }

    @BeforeEach void set_logger() {
        logger = Mockito.mock(Logger.class);
        LoggerMock.setMock(OpenFeatureClient.class, logger);
    }

    @AfterEach void reset_logs() {
        LoggerMock.setMock(OpenFeatureClient.class, logger);
    }

    @Specification(number="1.1.1", text="The API, and any state it maintains SHOULD exist as a global singleton, even in cases wherein multiple versions of the API are present at runtime.")
    @Test void global_singleton() {
        assertSame(OpenFeatureAPI.getInstance(), OpenFeatureAPI.getInstance());
    }

    @Specification(number="1.1.2.1", text="The API MUST define a provider mutator, a function to set the default provider, which accepts an API-conformant provider implementation.")
    @Test void provider() {
        FeatureProvider mockProvider = mock(FeatureProvider.class);
        FeatureProviderTestUtils.setFeatureProvider(mockProvider);
        assertThat(api.getProvider()).isEqualTo(mockProvider);
    }

    @SneakyThrows
    @Specification(number="1.1.8", text="The API SHOULD provide functions to set a provider and wait for the initialize function to return or throw.")
    @Test void providerAndWait() {
        FeatureProvider provider = new TestEventsProvider(500);
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        assertThat(api.getProvider().getState()).isEqualTo(ProviderState.READY);

        provider = new TestEventsProvider(500);
        String providerName = "providerAndWait";
        OpenFeatureAPI.getInstance().setProviderAndWait(providerName, provider);
        assertThat(api.getProvider(providerName).getState()).isEqualTo(ProviderState.READY);
    }

    @SneakyThrows
    @Specification(number="1.1.8", text="The API SHOULD provide functions to set a provider and wait for the initialize function to return or throw.")
    @Test void providerAndWaitError() {
        FeatureProvider provider1 = new TestEventsProvider(500, true, "fake error");
        assertThrows(GeneralError.class, () -> api.setProviderAndWait(provider1));

        FeatureProvider provider2 = new TestEventsProvider(500, true, "fake error");
        String providerName = "providerAndWaitError";
        assertThrows(GeneralError.class, () -> api.setProviderAndWait(providerName, provider2));
    }

    @Specification(number="2.4.5", text="The provider SHOULD indicate an error if flag resolution is attempted before the provider is ready.")
    @Test void shouldReturnNotReadyIfNotInitialized() {
        FeatureProvider provider = new InMemoryProvider(new HashMap<>()) {
            @Override
            public void initialize(EvaluationContext evaluationContext) throws Exception {
                Awaitility.await().wait(3000);
            }
        };
        String providerName = "shouldReturnNotReadyIfNotInitialized";
        OpenFeatureAPI.getInstance().setProvider(providerName, provider);
        assertThat(api.getProvider(providerName).getState()).isEqualTo(ProviderState.NOT_READY);
        Client client = OpenFeatureAPI.getInstance().getClient(providerName);
        assertEquals(ErrorCode.PROVIDER_NOT_READY, client.getBooleanDetails("return_error_when_not_initialized", false).getErrorCode());
    }

    @Specification(number="1.1.5", text="The API MUST provide a function for retrieving the metadata field of the configured provider.")
    @Test void provider_metadata() {
        FeatureProviderTestUtils.setFeatureProvider(new DoSomethingProvider());
        assertThat(api.getProviderMetadata().getName()).isEqualTo(DoSomethingProvider.name);
    }

    @Specification(number="1.1.4", text="The API MUST provide a function to add hooks which accepts one or more API-conformant hooks, and appends them to the collection of any previously added hooks. When new hooks are added, previously added hooks are not removed.")
    @Test void hook_addition() {
        Hook h1 = mock(Hook.class);
        Hook h2 = mock(Hook.class);
        api.addHooks(h1);

        assertEquals(1, api.getHooks().size());
        assertEquals(h1, api.getHooks().get(0));

        api.addHooks(h2);
        assertEquals(2, api.getHooks().size());
        assertEquals(h2, api.getHooks().get(1));
    }

    @Specification(number="1.1.6", text="The API MUST provide a function for creating a client which accepts the following options:  - name (optional): A logical string identifier for the client.")
    @Test void namedClient() {
        assertThatCode(() -> api.getClient("Sir Calls-a-lot")).doesNotThrowAnyException();
        // TODO: Doesn't say that you can *get* the client name.. which seems useful?
    }

    @Specification(number="1.2.1", text="The client MUST provide a method to add hooks which accepts one or more API-conformant hooks, and appends them to the collection of any previously added hooks. When new hooks are added, previously added hooks are not removed.")
    @Test void hookRegistration() {
        Client c = _client();
        Hook m1 = mock(Hook.class);
        Hook m2 = mock(Hook.class);
        c.addHooks(m1);
        c.addHooks(m2);
        List<Hook> hooks = c.getHooks();
        assertEquals(2, hooks.size());
        assertTrue(hooks.contains(m1));
        assertTrue(hooks.contains(m2));
    }

    @Specification(number="1.3.1.1", text="The client MUST provide methods for typed flag evaluation, including boolean, numeric, string, and structure, with parameters flag key (string, required), default value (boolean | number | string | structure, required), evaluation context (optional), and evaluation options (optional), which returns the flag value.")
    @Specification(number="1.3.3.1", text="The client SHOULD provide functions for floating-point numbers and integers, consistent with language idioms.")
    @Test void value_flags() {
        FeatureProviderTestUtils.setFeatureProvider(new DoSomethingProvider());

        Client c = api.getClient();
        String key = "key";

        assertEquals(true, c.getBooleanValue(key, false));
        assertEquals(true, c.getBooleanValue(key, false, new ImmutableContext()));
        assertEquals(true, c.getBooleanValue(key, false, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        assertEquals("gnirts-ym", c.getStringValue(key, "my-string"));
        assertEquals("gnirts-ym", c.getStringValue(key, "my-string", new ImmutableContext()));
        assertEquals("gnirts-ym", c.getStringValue(key, "my-string", new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        assertEquals(400, c.getIntegerValue(key, 4));
        assertEquals(400, c.getIntegerValue(key, 4, new ImmutableContext()));
        assertEquals(400, c.getIntegerValue(key, 4, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        assertEquals(40.0, c.getDoubleValue(key, .4));
        assertEquals(40.0, c.getDoubleValue(key, .4, new ImmutableContext()));
        assertEquals(40.0, c.getDoubleValue(key, .4, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        assertEquals(null, c.getObjectValue(key, new Value()));
        assertEquals(null, c.getObjectValue(key, new Value(), new ImmutableContext()));
        assertEquals(null, c.getObjectValue(key, new Value(), new ImmutableContext(), FlagEvaluationOptions.builder().build()));
    }

    @Specification(number="1.4.1.1", text="The client MUST provide methods for detailed flag value evaluation with parameters flag key (string, required), default value (boolean | number | string | structure, required), evaluation context (optional), and evaluation options (optional), which returns an evaluation details structure.")
    @Specification(number="1.4.3", text="The evaluation details structure's value field MUST contain the evaluated flag value.")
    @Specification(number="1.4.4.1", text="The evaluation details structure SHOULD accept a generic argument (or use an equivalent language feature) which indicates the type of the wrapped value field.")
    @Specification(number="1.4.5", text="The evaluation details structure's flag key field MUST contain the flag key argument passed to the detailed flag evaluation method.")
    @Specification(number="1.4.6", text="In cases of normal execution, the evaluation details structure's variant field MUST contain the value of the variant field in the flag resolution structure returned by the configured provider, if the field is set.")
    @Specification(number="1.4.7", text="In cases of normal execution, the `evaluation details` structure's `reason` field MUST contain the value of the `reason` field in the `flag resolution` structure returned by the configured `provider`, if the field is set.")
    @Test void detail_flags() {
        FeatureProviderTestUtils.setFeatureProvider(new DoSomethingProvider());
        Client c = api.getClient();
        String key = "key";

        FlagEvaluationDetails<Boolean> bd = FlagEvaluationDetails.<Boolean>builder()
                .flagKey(key)
                .value(false)
                .variant(null)
                .flagMetadata(DEFAULT_METADATA)
                .build();
        assertEquals(bd, c.getBooleanDetails(key, true));
        assertEquals(bd, c.getBooleanDetails(key, true, new ImmutableContext()));
        assertEquals(bd, c.getBooleanDetails(key, true, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<String> sd = FlagEvaluationDetails.<String>builder()
                .flagKey(key)
                .value("tset")
                .variant(null)
                .flagMetadata(DEFAULT_METADATA)
                .build();
        assertEquals(sd, c.getStringDetails(key, "test"));
        assertEquals(sd, c.getStringDetails(key, "test", new ImmutableContext()));
        assertEquals(sd, c.getStringDetails(key, "test", new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<Integer> id = FlagEvaluationDetails.<Integer>builder()
                .flagKey(key)
                .value(400)
                .flagMetadata(DEFAULT_METADATA)
                .build();
        assertEquals(id, c.getIntegerDetails(key, 4));
        assertEquals(id, c.getIntegerDetails(key, 4, new ImmutableContext()));
        assertEquals(id, c.getIntegerDetails(key, 4, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<Double> dd = FlagEvaluationDetails.<Double>builder()
                .flagKey(key)
                .value(40.0)
                .flagMetadata(DEFAULT_METADATA)
                .build();
        assertEquals(dd, c.getDoubleDetails(key, .4));
        assertEquals(dd, c.getDoubleDetails(key, .4, new ImmutableContext()));
        assertEquals(dd, c.getDoubleDetails(key, .4, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        // TODO: Structure detail tests.
    }

    @Specification(number="1.5.1", text="The evaluation options structure's hooks field denotes an ordered collection of hooks that the client MUST execute for the respective flag evaluation, in addition to those already configured.")
    @Test void hooks() {
        Client c = _client();
        Hook<Boolean> clientHook = mockBooleanHook();
        Hook<Boolean> invocationHook = mockBooleanHook();
        c.addHooks(clientHook);
        c.getBooleanValue("key", false, null, FlagEvaluationOptions.builder()
            .hook(invocationHook)
            .build());
        verify(clientHook, times(1)).before(any(), any());
        verify(invocationHook, times(1)).before(any(), any());
    }

    @Specification(number="1.4.8", text="In cases of abnormal execution, the `evaluation details` structure's `error code` field **MUST** contain an `error code`.")
    @Specification(number="1.4.9", text="In cases of abnormal execution (network failure, unhandled error, etc) the `reason` field in the `evaluation details` SHOULD indicate an error.")
    @Specification(number="1.4.10", text="Methods, functions, or operations on the client MUST NOT throw exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the `default value` in the event of abnormal execution. Exceptions include functions or methods for the purposes for configuration or setup.")
    @Specification(number="1.4.13", text="In cases of abnormal execution, the `evaluation details` structure's `error message` field **MAY** contain a string containing additional details about the nature of the error.")
    @Test void broken_provider() {
        FeatureProviderTestUtils.setFeatureProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();
        assertFalse(c.getBooleanValue("key", false));
        FlagEvaluationDetails<Boolean> details = c.getBooleanDetails("key", false);
        assertEquals(ErrorCode.FLAG_NOT_FOUND, details.getErrorCode());
        assertEquals(TestConstants.BROKEN_MESSAGE, details.getErrorMessage());
    }

    @Specification(number="1.4.11", text="In the case of abnormal execution, the client SHOULD log an informative error message.")
    @Test void log_on_error() throws NotImplementedException {
        FeatureProviderTestUtils.setFeatureProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();
        FlagEvaluationDetails<Boolean> result = c.getBooleanDetails("test", false);

        assertEquals(Reason.ERROR.toString(), result.getReason());
        Mockito.verify(logger).error(
                ArgumentMatchers.contains("Unable to correctly evaluate flag with key"),
                any(),
                ArgumentMatchers.isA(FlagNotFoundError.class));
    }

    @Specification(number="1.2.2", text="The client interface MUST define a metadata member or accessor, containing an immutable name field or accessor of type string, which corresponds to the name value supplied during client creation.")
    @Test void clientMetadata() {
        Client c = _client();
        assertNull(c.getMetadata().getName());

        FeatureProviderTestUtils.setFeatureProvider(new AlwaysBrokenProvider());
        Client c2 = api.getClient("test");
        assertEquals("test", c2.getMetadata().getName());
    }

    @Specification(number="1.4.9", text="In cases of abnormal execution (network failure, unhandled error, etc) the reason field in the evaluation details SHOULD indicate an error.")
    @Test void reason_is_error_when_there_are_errors() {
        FeatureProviderTestUtils.setFeatureProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();
        FlagEvaluationDetails<Boolean> result = c.getBooleanDetails("test", false);
        assertEquals(Reason.ERROR.toString(), result.getReason());
    }

    @Specification(number="1.4.14", text="If the flag metadata field in the flag resolution structure returned by the configured provider is set, the evaluation details structure's flag metadata field MUST contain that value. Otherwise, it MUST contain an empty record.")
    @Test void flag_metadata_passed() {
        FeatureProviderTestUtils.setFeatureProvider(new DoSomethingProvider(null));
        Client c = api.getClient();
        FlagEvaluationDetails<Boolean> result = c.getBooleanDetails("test", false);
        assertNotNull(result.getFlagMetadata());
    }

    @Specification(number="3.2.1.1", text="The API, Client and invocation MUST have a method for supplying evaluation context.")
    @Specification(number="3.2.2.1", text="The API MUST have a method for setting the global evaluation context.")
    @Specification(number="3.2.3", text="Evaluation context MUST be merged in the order: API (global; lowest precedence) -> transaction -> client -> invocation -> before hooks (highest precedence), with duplicate values being overwritten.")
    @Test void multi_layer_context_merges_correctly() {
        DoSomethingProvider provider = new DoSomethingProvider();
        FeatureProviderTestUtils.setFeatureProvider(provider);
        TransactionContextPropagator transactionContextPropagator = new ThreadLocalTransactionContextPropagator();
        api.setTransactionContextPropagator(transactionContextPropagator);

        Map<String, Value> apiAttributes = new HashMap<>();
        apiAttributes.put("common1", new Value("1"));
        apiAttributes.put("common2", new Value("1"));
        apiAttributes.put("common3", new Value("1"));
        apiAttributes.put("api", new Value("1"));
        EvaluationContext apiCtx = new ImmutableContext(apiAttributes);

        api.setEvaluationContext(apiCtx);

        Map<String, Value> transactionAttributes = new HashMap<>();
        // overwrite value from api context
        transactionAttributes.put("common1", new Value("2"));
        transactionAttributes.put("common4", new Value("2"));
        transactionAttributes.put("common5", new Value("2"));
        transactionAttributes.put("transaction", new Value("2"));
        EvaluationContext transactionCtx = new ImmutableContext(transactionAttributes);

        api.setTransactionContext(transactionCtx);

        Client c = api.getClient();
        Map<String, Value> clientAttributes = new HashMap<>();
        // overwrite value from api context
        clientAttributes.put("common2", new Value("3"));
        // overwrite value from transaction context
        clientAttributes.put("common4", new Value("3"));
        clientAttributes.put("common6", new Value("3"));
        clientAttributes.put("client", new Value("3"));
        EvaluationContext clientCtx = new ImmutableContext(clientAttributes);
        c.setEvaluationContext(clientCtx);

        Map<String, Value> invocationAttributes = new HashMap<>();
        // overwrite value from api context
        invocationAttributes.put("common3", new Value("4"));
        // overwrite value from transaction context
        invocationAttributes.put("common5", new Value("4"));
        // overwrite value from api client context
        invocationAttributes.put("common6", new Value("4"));
        invocationAttributes.put("invocation", new Value("4"));
        EvaluationContext invocationCtx = new ImmutableContext(invocationAttributes);

        // dosomethingprovider inverts this value.
        assertTrue(c.getBooleanValue("key", false, invocationCtx));

        EvaluationContext merged = provider.getMergedContext();
        assertEquals("1", merged.getValue("api").asString());
        assertEquals("2", merged.getValue("transaction").asString());
        assertEquals("3", merged.getValue("client").asString());
        assertEquals("4", merged.getValue("invocation").asString());
        assertEquals("2", merged.getValue("common1").asString(), "transaction merge is incorrect");
        assertEquals("3", merged.getValue("common2").asString(), "api client merge is incorrect");
        assertEquals("4", merged.getValue("common3").asString(), "invocation merge is incorrect");
        assertEquals("3", merged.getValue("common4").asString(), "api client merge is incorrect");
        assertEquals("4", merged.getValue("common5").asString(), "invocation merge is incorrect");
        assertEquals("4", merged.getValue("common6").asString(), "invocation merge is incorrect");

    }

    @Specification(number="3.3.1.1", text="The API SHOULD have a method for setting a transaction context propagator.")
    @Test void setting_transaction_context_propagator() {
        DoSomethingProvider provider = new DoSomethingProvider();
        FeatureProviderTestUtils.setFeatureProvider(provider);

        TransactionContextPropagator transactionContextPropagator = new ThreadLocalTransactionContextPropagator();
        api.setTransactionContextPropagator(transactionContextPropagator);
        assertEquals(transactionContextPropagator, api.getTransactionContextPropagator());
    }

    @Specification(number="3.3.1.2.1", text="The API MUST have a method for setting the evaluation context of the transaction context propagator for the current transaction.")
    @Test void setting_transaction_context() {
        DoSomethingProvider provider = new DoSomethingProvider();
        FeatureProviderTestUtils.setFeatureProvider(provider);

        TransactionContextPropagator transactionContextPropagator = new ThreadLocalTransactionContextPropagator();
        api.setTransactionContextPropagator(transactionContextPropagator);

        Map<String, Value> attributes = new HashMap<>();
        attributes.put("common", new Value("1"));
        EvaluationContext transactionContext = new ImmutableContext(attributes);

        api.setTransactionContext(transactionContext);
        assertEquals(transactionContext, transactionContextPropagator.getTransactionContext());
    }

    @Specification(number="3.3.1.2.2", text="A transaction context propagator MUST have a method for setting the evaluation context of the current transaction.")
    @Specification(number="3.3.1.2.3", text="A transaction context propagator MUST have a method for getting the evaluation context of the current transaction.")
    @Test void transaction_context_propagator_setting_context() {
        TransactionContextPropagator transactionContextPropagator = new ThreadLocalTransactionContextPropagator();

        Map<String, Value> attributes = new HashMap<>();
        attributes.put("common", new Value("1"));
        EvaluationContext transactionContext = new ImmutableContext(attributes);

        transactionContextPropagator.setTransactionContext(transactionContext);
        assertEquals(transactionContext, transactionContextPropagator.getTransactionContext());
    }

    @Specification(number="1.3.4", text="The client SHOULD guarantee the returned value of any typed flag evaluation method is of the expected type. If the value returned by the underlying provider implementation does not match the expected type, it's to be considered abnormal execution, and the supplied default value should be returned.")
    @Test void type_system_prevents_this() {}

    @Specification(number="1.1.7", text="The client creation function MUST NOT throw, or otherwise abnormally terminate.")
    @Test void constructor_does_not_throw() {}

    @Specification(number="1.4.12", text="The client SHOULD provide asynchronous or non-blocking mechanisms for flag evaluation.")
    @Test void one_thread_per_request_model() {}

    @Specification(number="1.4.14.1", text="Condition: Flag metadata MUST be immutable.")
    @Test void compiler_enforced() {}

    @Specification(number="1.4.2.1", text="The client MUST provide methods for detailed flag value evaluation with parameters flag key (string, required), default value (boolean | number | string | structure, required), and evaluation options (optional), which returns an evaluation details structure.")
    @Specification(number="1.3.2.1", text="The client MUST provide methods for typed flag evaluation, including boolean, numeric, string, and structure, with parameters flag key (string, required), default value (boolean | number | string | structure, required), and evaluation options (optional), which returns the flag value.")
    @Specification(number="3.2.2.2", text="The Client and invocation MUST NOT have a method for supplying evaluation context.")
    @Specification(number="3.2.4.1", text="When the global evaluation context is set, the on context changed handler MUST run.")
    @Specification(number="3.3.2.1", text="The API MUST NOT have a method for setting a transaction context propagator.")
    @Test void not_applicable_for_dynamic_context() {}

}
