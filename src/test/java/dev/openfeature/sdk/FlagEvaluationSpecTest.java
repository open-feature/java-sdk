package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.openfeature.sdk.fixtures.HookFixtures;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.simplify4u.slf4jmock.LoggerMock;
import org.slf4j.Logger;

class FlagEvaluationSpecTest implements HookFixtures {

    private Logger logger;

    private Client _client() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        return api.getClient();
    }

    @AfterEach void reset_ctx() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
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

    @Specification(number="1.1.2", text="The API MUST provide a function to set the global provider singleton, which accepts an API-conformant provider implementation.")
    @Test void provider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProvider mockProvider = mock(FeatureProvider.class);
        api.setProvider(mockProvider);
        assertEquals(mockProvider, api.getProvider());
    }

    @Specification(number="1.1.4", text="The API MUST provide a function for retrieving the metadata field of the configured provider.")
    @Test void provider_metadata() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new DoSomethingProvider());
        assertEquals(DoSomethingProvider.name, api.getProviderMetadata().getName());
    }

    @Specification(number="1.1.3", text="The API MUST provide a function to add hooks which accepts one or more API-conformant hooks, and appends them to the collection of any previously added hooks. When new hooks are added, previously added hooks are not removed.")
    @Test void hook_addition() {
        Hook h1 = mock(Hook.class);
        Hook h2 = mock(Hook.class);
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.addHooks(h1);

        assertEquals(1, api.getHooks().size());
        assertEquals(h1, api.getHooks().get(0));

        api.addHooks(h2);
        assertEquals(2, api.getHooks().size());
        assertEquals(h2, api.getHooks().get(1));
    }

    @Specification(number="1.1.5", text="The API MUST provide a function for creating a client which accepts the following options:  - name (optional): A logical string identifier for the client.")
    @Test void namedClient() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        Client c = api.getClient("Sir Calls-a-lot");
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

    @Specification(number="1.3.1", text="The client MUST provide methods for typed flag evaluation, including boolean, numeric, string, and structure, with parameters flag key (string, required), default value (boolean | number | string | structure, required), evaluation context (optional), and evaluation options (optional), which returns the flag value.")
    @Specification(number="1.3.2.1", text="The client SHOULD provide functions for floating-point numbers and integers, consistent with language idioms.")
    @Test void value_flags() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new DoSomethingProvider());
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

    @Specification(number="1.4.1", text="The client MUST provide methods for detailed flag value evaluation with parameters flag key (string, required), default value (boolean | number | string | structure, required), evaluation context (optional), and evaluation options (optional), which returns an evaluation details structure.")
    @Specification(number="1.4.2", text="The evaluation details structure's value field MUST contain the evaluated flag value.")
    @Specification(number="1.4.3.1", text="The evaluation details structure SHOULD accept a generic argument (or use an equivalent language feature) which indicates the type of the wrapped value field.")
    @Specification(number="1.4.4", text="The evaluation details structure's flag key field MUST contain the flag key argument passed to the detailed flag evaluation method.")
    @Specification(number="1.4.5", text="In cases of normal execution, the evaluation details structure's variant field MUST contain the value of the variant field in the flag resolution structure returned by the configured provider, if the field is set.")
    @Specification(number="1.4.6", text="In cases of normal execution, the evaluation details structure's reason field MUST contain the value of the reason field in the flag resolution structure returned by the configured provider, if the field is set.")
    @Test void detail_flags() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new DoSomethingProvider());
        Client c = api.getClient();
        String key = "key";

        FlagEvaluationDetails<Boolean> bd = FlagEvaluationDetails.<Boolean>builder()
                .flagKey(key)
                .value(false)
                .variant(null)
                .build();
        assertEquals(bd, c.getBooleanDetails(key, true));
        assertEquals(bd, c.getBooleanDetails(key, true, new ImmutableContext()));
        assertEquals(bd, c.getBooleanDetails(key, true, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<String> sd = FlagEvaluationDetails.<String>builder()
                .flagKey(key)
                .value("tset")
                .variant(null)
                .build();
        assertEquals(sd, c.getStringDetails(key, "test"));
        assertEquals(sd, c.getStringDetails(key, "test", new ImmutableContext()));
        assertEquals(sd, c.getStringDetails(key, "test", new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<Integer> id = FlagEvaluationDetails.<Integer>builder()
                .flagKey(key)
                .value(400)
                .build();
        assertEquals(id, c.getIntegerDetails(key, 4));
        assertEquals(id, c.getIntegerDetails(key, 4, new ImmutableContext()));
        assertEquals(id, c.getIntegerDetails(key, 4, new ImmutableContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<Double> dd = FlagEvaluationDetails.<Double>builder()
                .flagKey(key)
                .value(40.0)
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

    @Specification(number="1.4.9", text="Methods, functions, or operations on the client MUST NOT throw exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the default value in the event of abnormal execution. Exceptions include functions or methods for the purposes for configuration or setup.")
    @Specification(number="1.4.7", text="In cases of abnormal execution, the `evaluation details` structure's `error code` field **MUST** contain an `error code`.")
    @Specification(number="1.4.12", text="In cases of abnormal execution, the `evaluation details` structure's `error message` field **MAY** contain a string containing additional details about the nature of the error.")
    @Test void broken_provider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();
        assertFalse(c.getBooleanValue("key", false));
        FlagEvaluationDetails<Boolean> details = c.getBooleanDetails("key", false);
        assertEquals(ErrorCode.FLAG_NOT_FOUND, details.getErrorCode());
        assertEquals(TestConstants.BROKEN_MESSAGE, details.getErrorMessage());
    }

    @Specification(number="1.4.10", text="In the case of abnormal execution, the client SHOULD log an informative error message.")
    @Test void log_on_error() throws NotImplementedException {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
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

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client c2 = api.getClient("test");
        assertEquals("test", c2.getMetadata().getName());
    }

    @Specification(number="1.4.8", text="In cases of abnormal execution (network failure, unhandled error, etc) the reason field in the evaluation details SHOULD indicate an error.")
    @Test void reason_is_error_when_there_are_errors() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();
        FlagEvaluationDetails<Boolean> result = c.getBooleanDetails("test", false);
        assertEquals(Reason.ERROR.toString(), result.getReason());
    }

    @Specification(number="3.2.1", text="The API, Client and invocation MUST have a method for supplying evaluation context.")
    @Specification(number="3.2.2", text="Evaluation context MUST be merged in the order: API (global; lowest precedence) - client - invocation - before hooks (highest precedence), with duplicate values being overwritten.")
    @Test void multi_layer_context_merges_correctly() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        DoSomethingProvider provider = new DoSomethingProvider();
        api.setProvider(provider);

        Map<String, Value> attributes = new HashMap<>();
        attributes.put("common", new Value("1"));
        attributes.put("common2", new Value("1"));
        attributes.put("api", new Value("2"));
        EvaluationContext apiCtx = new ImmutableContext(attributes);

        api.setEvaluationContext(apiCtx);

        Client c = api.getClient();
        Map<String, Value> attributes1 = new HashMap<>();
        attributes.put("common", new Value("3"));
        attributes.put("common2", new Value("3"));
        attributes.put("client", new Value("4"));
        attributes.put("common", new Value("5"));
        attributes.put("invocation", new Value("6"));
        EvaluationContext clientCtx = new ImmutableContext(attributes);
        c.setEvaluationContext(clientCtx);

        EvaluationContext invocationCtx = new ImmutableContext();

        // dosomethingprovider inverts this value.
        assertTrue(c.getBooleanValue("key", false, invocationCtx));

        EvaluationContext merged = provider.getMergedContext();
        assertEquals("6", merged.getValue("invocation").asString());
        assertEquals("5", merged.getValue("common").asString(), "invocation merge is incorrect");
        assertEquals("4", merged.getValue("client").asString());
        assertEquals("3", merged.getValue("common2").asString(), "api client merge is incorrect");
        assertEquals("2", merged.getValue("api").asString());

    }

    @Specification(number="1.3.3", text="The client SHOULD guarantee the returned value of any typed flag evaluation method is of the expected type. If the value returned by the underlying provider implementation does not match the expected type, it's to be considered abnormal execution, and the supplied default value should be returned.")
    @Test void type_system_prevents_this() {}

    @Specification(number="1.1.6", text="The client creation function MUST NOT throw, or otherwise abnormally terminate.")
    @Test void constructor_does_not_throw() {}

    @Specification(number="1.4.11", text="The client SHOULD provide asynchronous or non-blocking mechanisms for flag evaluation.")
    @Test void one_thread_per_request_model() {}
}
