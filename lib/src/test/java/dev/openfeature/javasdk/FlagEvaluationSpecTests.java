package dev.openfeature.javasdk;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FlagEvaluationSpecTests {

    Client _client() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        return api.getClient();
    }

    @Test void global_singleton() {
        assertSame(OpenFeatureAPI.getInstance(), OpenFeatureAPI.getInstance());
    }

    @Test void provider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProvider mockProvider = mock(FeatureProvider.class);
        api.setProvider(mockProvider);
        assertEquals(mockProvider, api.getProvider());
    }

    @Test void hook_addition() {
        Hook h1 = mock(Hook.class);
        Hook h2 = mock(Hook.class);
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.addHooks(h1);

        assertEquals(1, api.getApiHooks().size());
        assertEquals(h1, api.getApiHooks().get(0));

        api.addHooks(h2);
        assertEquals(2, api.getApiHooks().size());
        assertEquals(h2, api.getApiHooks().get(1));
    }

    @Test void namedClient() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        Client c = api.getClient("Sir Calls-a-lot");
        // TODO: Doesn't say that you can *get* the client name.. which seems useful?
    }

    @Test void hookRegistration() {
        Client c = _client();
        Hook m1 = mock(Hook.class);
        Hook m2 = mock(Hook.class);
        c.addHooks(m1);
        c.addHooks(m2);
        List<Hook> hooks = c.getClientHooks();
        assertEquals(2, hooks.size());
        assertTrue(hooks.contains(m1));
        assertTrue(hooks.contains(m2));
    }

    @Test void value_flags() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new DoSomethingProvider());
        Client c = api.getClient();
        String key = "key";

        assertEquals(true, c.getBooleanValue(key, false));
        assertEquals(true, c.getBooleanValue(key, false, new EvaluationContext()));
        assertEquals(true, c.getBooleanValue(key, false, new EvaluationContext(), FlagEvaluationOptions.builder().build()));


        assertEquals("gnirts-ym", c.getStringValue(key, "my-string"));
        assertEquals("gnirts-ym", c.getStringValue(key, "my-string", new EvaluationContext()));
        assertEquals("gnirts-ym", c.getStringValue(key, "my-string", new EvaluationContext(), FlagEvaluationOptions.builder().build()));

        assertEquals(400, c.getIntegerValue(key, 4));
        assertEquals(400, c.getIntegerValue(key, 4, new EvaluationContext()));
        assertEquals(400, c.getIntegerValue(key, 4, new EvaluationContext(), FlagEvaluationOptions.builder().build()));

        assertEquals(null, c.getObjectValue(key, new Node<Integer>()));
        assertEquals(null, c.getObjectValue(key, new Node<Integer>(), new EvaluationContext()));
        assertEquals(null, c.getObjectValue(key, new Node<Integer>(), new EvaluationContext(), FlagEvaluationOptions.builder().build()));
    }

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
        assertEquals(bd, c.getBooleanDetails(key, true, new EvaluationContext()));
        assertEquals(bd, c.getBooleanDetails(key, true, new EvaluationContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<String> sd = FlagEvaluationDetails.<String>builder()
                .flagKey(key)
                .value("tset")
                .variant(null)
                .build();
        assertEquals(sd, c.getStringDetails(key, "test"));
        assertEquals(sd, c.getStringDetails(key, "test", new EvaluationContext()));
        assertEquals(sd, c.getStringDetails(key, "test", new EvaluationContext(), FlagEvaluationOptions.builder().build()));

        FlagEvaluationDetails<Integer> id = FlagEvaluationDetails.<Integer>builder()
                .flagKey(key)
                .value(400)
                .build();
        assertEquals(id, c.getIntegerDetails(key, 4));
        assertEquals(id, c.getIntegerDetails(key, 4, new EvaluationContext()));
        assertEquals(id, c.getIntegerDetails(key, 4, new EvaluationContext(), FlagEvaluationOptions.builder().build()));
    }

    @Test void hooks() {
        Client c = _client();
        Hook clientHook = mock(Hook.class);
        Hook invocationHook = mock(Hook.class);
        c.addHooks(clientHook);
        c.getBooleanValue("key", false, null, FlagEvaluationOptions.builder()
                        .hook(invocationHook)
                        .build());
        verify(clientHook, times(1)).before(any(), any());
        verify(invocationHook, times(1)).before(any(), any());
    }

    @Test void broken_provider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();
        assertFalse(c.getBooleanValue("key", false));

    }
    @Disabled("Not actually sure how to mock out the slf4j logger")
    @Test void log_on_error() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Specification(number="1.1.1", text="The API, and any state it maintains SHOULD exist as a global singleton, even in cases wherein multiple versions of the API are present at runtime.")
    @Specification(number="1.1.2", text="The API MUST provide a function to set the global provider singleton, which accepts an API-conformant provider implementation.")
    @Specification(number="1.1.3", text="The API MUST provide a function to add hooks which accepts one or more API-conformant hooks, and appends them to the collection of any previously added hooks. When new hooks are added, previously added hooks are not removed.")
    @Specification(number="1.1.4", text="The API MUST provide a function for retrieving the metadata field of the configured provider.")
    @Specification(number="1.1.5", text="The API MUST provide a function for creating a client which accepts the following options:  - name (optional): A logical string identifier for the client.")
    @Specification(number="1.1.6", text="The client creation function MUST NOT throw, or otherwise abnormally terminate.")
    @Specification(number="1.2.1", text="The client MUST provide a method to add hooks which accepts one or more API-conformant hooks, and appends them to the collection of any previously added hooks. When new hooks are added, previously added hooks are not removed.")
    @Specification(number="1.2.2", text="The client interface MUST define a metadata member or accessor, containing an immutable name field or accessor of type string, which corresponds to the name value supplied during client creation.")
    @Specification(number="1.3.1", text="The client MUST provide methods for flag evaluation, with parameters flag key (string, required), default value (boolean | number | string | structure, required), evaluation context (optional), and evaluation options (optional), which returns the flag value.")
    @Specification(number="1.3.2.1", text="The client MUST provide methods for typed flag evaluation, including boolean, numeric, string, and structure.")
    @Specification(number="1.3.3", text="The client SHOULD guarantee the returned value of any typed flag evaluation method is of the expected type. If the value returned by the underlying provider implementation does not match the expected type, it's to be considered abnormal execution, and the supplied default value should be returned.")
    @Specification(number="1.4.1", text="The client MUST provide methods for detailed flag value evaluation with parameters flag key (string, required), default value (boolean | number | string | structure, required), evaluation context (optional), and evaluation options (optional), which returns an evaluation details structure.")
    @Specification(number="1.4.2", text="The evaluation details structure's value field MUST contain the evaluated flag value.")
    @Specification(number="1.4.3.1", text="The evaluation details structure SHOULD accept a generic argument (or use an equivalent language feature) which indicates the type of the wrapped value field.")
    @Specification(number="1.4.4", text="The evaluation details structure's flag key field MUST contain the flag key argument passed to the detailed flag evaluation method.")
    @Specification(number="1.4.5", text="In cases of normal execution, the evaluation details structure's variant field MUST contain the value of the variant field in the flag resolution structure returned by the configured provider, if the field is set.")
    @Specification(number="1.4.6", text="In cases of normal execution, the evaluation details structure's reason field MUST contain the value of the reason field in the flag resolution structure returned by the configured provider, if the field is set.")
    @Specification(number="1.4.7", text="In cases of abnormal execution, the evaluation details structure's error code field MUST contain a string identifying an error occurred during flag evaluation and the nature of the error.")
    @Specification(number="1.4.8", text="In cases of abnormal execution (network failure, unhandled error, etc) the reason field in the evaluation details SHOULD indicate an error.")
    @Specification(number="1.4.9", text="Methods, functions, or operations on the client MUST NOT throw exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the default value in the event of abnormal execution. Exceptions include functions or methods for the purposes for configuration or setup.")
    @Specification(number="1.4.10", text="In the case of abnormal execution, the client SHOULD log an informative error message.")
    @Specification(number="1.4.11", text="The client SHOULD provide asynchronous or non-blocking mechanisms for flag evaluation.")
    @Specification(number="1.5.1", text="The evaluation options structure's hooks field denotes an ordered collection of hooks that the client MUST execute for the respective flag evaluation, in addition to those already configured.")
    @Specification(number="1.6.1", text="The client SHOULD transform the evaluation context using the provider's context transformer function if one is defined, before passing the result of the transformation to the provider's flag resolution functions.")

    @Test @Disabled void todo() {}

    @Disabled("We're operating in a one request per thread model")
    @Test void explicitly_not_doing() {

    }
}
