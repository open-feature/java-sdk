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

    @Specification(spec="flag evaluation", number="1.1",
            text="The API, and any state it maintains SHOULD exist as a global singleton, " +
            "even in cases wherein multiple versions of the API are present at runtime.")
    @Test void global_singleton() {
        assertSame(OpenFeatureAPI.getInstance(), OpenFeatureAPI.getInstance());
    }

    @Specification(spec="flag evaluation", number="1.2",
    text="The API MUST provide a function to set the global provider singleton, " +
            "which accepts an API-conformant provider implementation.")
    @Specification(spec="flag evaluation", number="1.4",
    text="The API MUST provide a function for retrieving the provider implementation.")
    @Test void provider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProvider mockProvider = mock(FeatureProvider.class);
        api.setProvider(mockProvider);
        assertEquals(mockProvider, api.getProvider());
    }

    @Specification(spec="flag evaluation", number="1.3", text="The API MUST provide a function to add hooks which " +
            "accepts one or more API-conformant hooks, and appends them to the collection of any previously added hooks." +
            "When new hooks are added, previously added hooks are not removed.")
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

    @Specification(spec="flag evaluation", number="1.5", text="The API MUST provide a function for creating a client " +
            "which accepts the following options: name (optional): A logical string identifier for the client.")
    @Test void namedClient() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        Client c = api.getClient("Sir Calls-a-lot");
        // TODO: Doesn't say that you can *get* the client name.. which seems useful?
    }

    @Specification(spec="flag evaluation", number="1.6", text="The client MUST provide a method to add hooks which " +
            "accepts one or more API-conformant hooks, and appends them to the collection of any previously added hooks. " +
            "When new hooks are added, previously added hooks are not removed.")
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

    @Specification(spec="flag evaluation", number="1.7", text="The client MUST provide methods for flag evaluation, with" +
            " parameters flag key (string, required), default value (boolean | number | string | structure, required), " +
            "evaluation context (optional), and evaluation options (optional), which returns the flag value.")
    @Specification(spec="flag evaluation", number="1.8.1",text="The client MUST provide methods for typed flag " +
            "evaluation, including boolean, numeric, string, and structure.")
    @Test void value_flags() {
        Client c = _client();
        String key = "key";
        assertFalse(c.getBooleanValue(key, false));
        assertFalse(c.getBooleanValue(key, false, new EvaluationContext()));
        assertFalse(c.getBooleanValue(key, false, new EvaluationContext(), FlagEvaluationOptions.builder().build()));


        assertEquals("my-string", c.getStringValue(key, "my-string"));
        assertEquals("my-string", c.getStringValue(key, "my-string", new EvaluationContext()));
        assertEquals("my-string", c.getStringValue(key, "my-string", new EvaluationContext(), FlagEvaluationOptions.builder().build()));

        assertEquals(4, c.getIntegerValue(key, 4));
        assertEquals(4, c.getIntegerValue(key, 4, new EvaluationContext()));
        assertEquals(4, c.getIntegerValue(key, 4, new EvaluationContext(), FlagEvaluationOptions.builder().build()));

    }

    @Specification(spec="flag evaluation", number="1.7", text="The client MUST provide methods for flag evaluation, with" +
            " parameters flag key (string, required), default value (boolean | number | string | structure, required), " +
            "evaluation context (optional), and evaluation options (optional), which returns the flag value.")
    @Disabled
    @Test void value_flags__object() {
        throw new NotImplementedException();
    }


    @Specification(spec="flag evaluation", number="1.9", text="The client MUST provide methods for detailed flag value " +
            "evaluation with parameters flag key (string, required), default value (boolean | number | string | " +
            "structure, required), evaluation context (optional), and evaluation options (optional), which returns an " +
            "evaluation details structure.")
    @Specification(spec="flag evaluation", number="1.10", text="The evaluation details structure's value field MUST " +
            "contain the evaluated flag value.")
    @Specification(spec="flag evaluation", number="1.11.1", text="The evaluation details structure SHOULD accept a " +
            "generic argument (or use an equivalent language feature) which indicates the type of the wrapped value " +
            "field.")
    @Specification(spec="flag evaluation", number="1.12", text="The evaluation details structure's flag key field MUST " +
            "contain the flag key argument passed to the detailed flag evaluation method.")
    @Specification(spec="flag evaluation", number="1.13", text="In cases of normal execution, the evaluation details " +
            "structure's variant field MUST contain the value of the variant field in the flag resolution structure " +
            "returned by the configured provider, if the field is set.")
    @Specification(spec="flag evaluation", number="1.14", text="In cases of normal execution, the evaluation details " +
            "structure's reason field MUST contain the value of the reason field in the flag resolution structure " +
            "returned by the configured provider, if the field is set.")
    @Specification(spec="flag evaluation", number="1.15", text="In cases of abnormal execution, the evaluation details " +
            "structure's error code field MUST identify an error occurred during flag evaluation, having possible " +
            "values PROVIDER_NOT_READY, FLAG_NOT_FOUND, PARSE_ERROR, TYPE_MISMATCH, or GENERAL.")
    @Specification(spec="flag evaluation", number="1.16", text="In cases of abnormal execution (network failure, " +
            "unhandled error, etc) the reason field in the evaluation details SHOULD indicate an error.")
    @Disabled
    @Test void detail_flags() {
        // TODO: Add tests re: detail functions.
        throw new NotImplementedException();
    }

    @Specification(spec="flag evaluation", number="1.17", text="The evaluation options structure's hooks field denotes " +
            "a collection of hooks that the client MUST execute for the respective flag evaluation, in addition to " +
            "those already configured.")
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

    @Specification(spec="flag evaluation", number="1.18", text="Methods, functions, or operations on the client MUST " +
            "NOT throw exceptions, or otherwise abnormally terminate. Flag evaluation calls must always return the " +
            "default value in the event of abnormal execution. Exceptions include functions or methods for the " +
            "purposes for configuration or setup.")
    @Test void broken_provider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();
        assertFalse(c.getBooleanValue("key", false));

    }
    @Specification(spec="flag evaluation", number="1.19", text="In the case of abnormal execution, the client SHOULD " +
            "log an informative error message.")
    @Disabled("Not actually sure how to mock out the slf4j logger")
    @Test void log_on_error() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Specification(spec="flag evaluation", number="1.21", text="The client MUST transform the evaluation context using " +
            "the provider's context transformer function, before passing the result of the transformation to the " +
            "provider's flag resolution functions.")
    @Test @Disabled void todo() {}

    @Specification(spec="flag evaluation", number="1.20", text="The client SHOULD provide asynchronous or non-blocking " +
            "mechanisms for flag evaluation.")
    @Disabled("We're operating in a one request per thread model")
    @Test void explicitly_not_doing() {

    }
}
