package dev.openfeature.sdk;

import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.fixtures.HookFixtures;
import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;
import dev.openfeature.sdk.testutils.TestEventsProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HookSpecTest implements HookFixtures {
    @AfterEach
    void emptyApiHooks() {
        // it's a singleton. Don't pollute each test.
        OpenFeatureAPI.getInstance().clearHooks();
    }

    @Specification(number = "4.1.3", text = "The flag key, flag type, and default value properties MUST be immutable. If the language does not support immutability, the hook MUST NOT modify these properties.")
    @Test
    void immutableValues() {
        try {
            HookContext.class.getMethod("setFlagKey");
            fail("Shouldn't be able to find this method");
        } catch (NoSuchMethodException e) {
            // expected
        }

        try {
            HookContext.class.getMethod("setType");
            fail("Shouldn't be able to find this method");
        } catch (NoSuchMethodException e) {
            // expected
        }

        try {
            HookContext.class.getMethod("setDefaultValue");
            fail("Shouldn't be able to find this method");
        } catch (NoSuchMethodException e) {
            // expected
        }
    }

    @Specification(number = "4.1.1", text = "Hook context MUST provide: the flag key, flag value type, evaluation context, and the default value.")
    @Test
    void nullish_properties_on_hookcontext() {
        // missing ctx
        try {
            HookContext.<Integer>builder()
                    .flagKey("key")
                    .type(FlagValueType.INTEGER)
                    .defaultValue(1)
                    .build();
            fail("Missing context shouldn't be valid");
        } catch (NullPointerException e) {
            // expected
        }

        // missing type
        try {
            HookContext.<Integer>builder()
                    .flagKey("key")
                    .ctx(null)
                    .defaultValue(1)
                    .build();
            fail("Missing type shouldn't be valid");
        } catch (NullPointerException e) {
            // expected
        }

        // missing key
        try {
            HookContext.<Integer>builder()
                    .type(FlagValueType.INTEGER)
                    .ctx(null)
                    .defaultValue(1)
                    .build();
            fail("Missing key shouldn't be valid");
        } catch (NullPointerException e) {
            // expected
        }

        // missing default value
        try {
            HookContext.<Integer>builder()
                    .flagKey("key")
                    .type(FlagValueType.INTEGER)
                    .ctx(new ImmutableContext())
                    .build();
            fail("Missing default value shouldn't be valid");
        } catch (NullPointerException e) {
            // expected
        }

        // normal
        try {
            HookContext.<Integer>builder()
                    .flagKey("key")
                    .type(FlagValueType.INTEGER)
                    .ctx(new ImmutableContext())
                    .defaultValue(1)
                    .build();
        } catch (NullPointerException e) {
            fail("NPE after we provided all relevant info");
        }

    }

    @Specification(number = "4.1.2", text = "The hook context SHOULD provide: access to the client metadata and the provider metadata fields.")
    @Test
    void optional_properties() {
        // don't specify
        HookContext.<Integer>builder()
                .flagKey("key")
                .type(FlagValueType.INTEGER)
                .ctx(new ImmutableContext())
                .defaultValue(1)
                .build();

        // add optional provider
        HookContext.<Integer>builder()
                .flagKey("key")
                .type(FlagValueType.INTEGER)
                .ctx(new ImmutableContext())
                .providerMetadata(new NoOpProvider().getMetadata())
                .defaultValue(1)
                .build();

        // add optional client
        HookContext.<Integer>builder()
                .flagKey("key")
                .type(FlagValueType.INTEGER)
                .ctx(new ImmutableContext())
                .defaultValue(1)
                .clientMetadata(OpenFeatureAPI.getInstance().getClient().getMetadata())
                .build();
    }

    @Specification(number = "4.3.2.1", text = "The before stage MUST run before flag resolution occurs. It accepts a hook context (required) and hook hints (optional) as parameters and returns either an evaluation context or nothing.")
    @Test
    void before_runs_ahead_of_evaluation() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProviderAndWait(new AlwaysBrokenProvider());
        Client client = api.getClient();
        Hook<Boolean> evalHook = mockBooleanHook();

        client.getBooleanValue("key", false, new ImmutableContext(),
                FlagEvaluationOptions.builder().hook(evalHook).build());

        verify(evalHook, times(1)).before(any(), any());
    }

    @Test
    void feo_has_hook_list() {
        FlagEvaluationOptions feo = FlagEvaluationOptions.builder()
                .build();
        assertNotNull(feo.getHooks());
    }

    @Test
    void error_hook_run_during_non_finally_stage() {
        final boolean[] error_called = {false};
        Hook h = mockBooleanHook();
        doThrow(RuntimeException.class).when(h).finallyAfter(any(), any());

        verify(h, times(0)).error(any(), any(), any());
    }


    @Test
    void error_hook_must_run_if_resolution_details_returns_an_error_code() {

        String errorMessage = "not found...";

        EvaluationContext invocationCtx = new ImmutableContext();
        Hook<Boolean> hook = mockBooleanHook();
        FeatureProvider provider = mock(FeatureProvider.class);
        when(provider.getBooleanEvaluation(any(), any(), any())).thenReturn(ProviderEvaluation.<Boolean>builder()
                .errorCode(ErrorCode.FLAG_NOT_FOUND)
                .errorMessage(errorMessage)
                .build());

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProviderTestUtils.setFeatureProvider(provider);
        Client client = api.getClient();
        client.getBooleanValue("key", false, invocationCtx,
                FlagEvaluationOptions.builder()
                        .hook(hook)
                        .build());

        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);

        verify(hook, times(1)).before(any(), any());
        verify(hook, times(1)).error(any(), captor.capture(), any());
        verify(hook, times(1)).finallyAfter(any(), any());
        verify(hook, never()).after(any(), any(), any());

        Exception exception = captor.getValue();
        assertEquals(errorMessage, exception.getMessage());
        assertInstanceOf(FlagNotFoundError.class, exception);
    }


    @Specification(number = "4.3.6", text = "The after stage MUST run after flag resolution occurs. It accepts a hook context (required), flag evaluation details (required) and hook hints (optional). It has no return value.")
    @Specification(number = "4.3.7", text = "The error hook MUST run when errors are encountered in the before stage, the after stage or during flag resolution. It accepts hook context (required), exception representing what went wrong (required), and hook hints (optional). It has no return value.")
    @Specification(number = "4.3.8", text = "The finally hook MUST run after the before, after, and error stages. It accepts a hook context (required) and hook hints (optional). There is no return value.")
    @Specification(number = "4.4.1", text = "The API, Client, Provider, and invocation MUST have a method for registering hooks.")
    @Specification(number = "4.4.2", text = "Hooks MUST be evaluated in the following order:  - before: API, Client, Invocation, Provider - after: Provider, Invocation, Client, API - error (if applicable): Provider, Invocation, Client, API - finally: Provider, Invocation, Client, API")
    @Test
    void hook_eval_order() {
        List<String> evalOrder = new ArrayList<>();
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider("evalOrder", new TestEventsProvider() {
            public List<Hook> getProviderHooks() {
                return Collections.singletonList(new BooleanHook() {

                    @Override
                    public Optional<EvaluationContext> before(HookContext<Boolean> ctx, Map<String, Object> hints) {
                        evalOrder.add("provider before");
                        return null;
                    }

                    @Override
                    public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, Map<String,
                            Object> hints) {
                        evalOrder.add("provider after");
                    }

                    @Override
                    public void error(HookContext<Boolean> ctx, Exception error, Map<String, Object> hints) {
                        evalOrder.add("provider error");
                    }

                    @Override
                    public void finallyAfter(HookContext<Boolean> ctx, Map<String, Object> hints) {
                        evalOrder.add("provider finally");
                    }
                });
            }
        });
        api.addHooks(new BooleanHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, Map<String, Object> hints) {
                evalOrder.add("api before");
                return null;
            }

            @Override
            public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, Map<String, Object> hints) {
                evalOrder.add("api after");
                throw new RuntimeException(); // trigger error flows.
            }

            @Override
            public void error(HookContext<Boolean> ctx, Exception error, Map<String, Object> hints) {
                evalOrder.add("api error");
            }

            @Override
            public void finallyAfter(HookContext<Boolean> ctx, Map<String, Object> hints) {
                evalOrder.add("api finally");
            }
        });

        Client c = api.getClient("evalOrder");
        c.addHooks(new BooleanHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, Map<String, Object> hints) {
                evalOrder.add("client before");
                return null;
            }

            @Override
            public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, Map<String, Object> hints) {
                evalOrder.add("client after");
            }

            @Override
            public void error(HookContext<Boolean> ctx, Exception error, Map<String, Object> hints) {
                evalOrder.add("client error");
            }

            @Override
            public void finallyAfter(HookContext<Boolean> ctx, Map<String, Object> hints) {
                evalOrder.add("client finally");
            }
        });

        c.getBooleanValue("key", false, null, FlagEvaluationOptions
                .builder()
                .hook(new BooleanHook() {
                    @Override
                    public Optional<EvaluationContext> before(HookContext<Boolean> ctx, Map<String, Object> hints) {
                        evalOrder.add("invocation before");
                        return null;
                    }

                    @Override
                    public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, Map<String, Object> hints) {
                        evalOrder.add("invocation after");
                    }

                    @Override
                    public void error(HookContext<Boolean> ctx, Exception error, Map<String, Object> hints) {
                        evalOrder.add("invocation error");
                    }

                    @Override
                    public void finallyAfter(HookContext<Boolean> ctx, Map<String, Object> hints) {
                        evalOrder.add("invocation finally");
                    }
                })
                .build());

        List<String> expectedOrder = Arrays.asList(
                "api before", "client before", "invocation before", "provider before",
                "provider after", "invocation after", "client after", "api after",
                "provider error", "invocation error", "client error", "api error",
                "provider finally", "invocation finally", "client finally", "api finally");
        assertEquals(expectedOrder, evalOrder);
    }

    @Specification(number = "4.4.6", text = "If an error occurs during the evaluation of before or after hooks, any remaining hooks in the before or after stages MUST NOT be invoked.")
    @Test
    void error_stops_before() {
        Hook<Boolean> h = mockBooleanHook();
        doThrow(RuntimeException.class).when(h).before(any(), any());
        Hook<Boolean> h2 = mockBooleanHook();

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProviderAndWait(new AlwaysBrokenProvider());
        Client c = api.getClient();

        c.getBooleanDetails("key", false, null, FlagEvaluationOptions.builder()
                .hook(h2)
                .hook(h)
                .build());
            verify(h, times(1)).before(any(), any());
            verify(h2, times(0)).before(any(), any());
    }

    @Specification(number = "4.4.6", text = "If an error occurs during the evaluation of before or after hooks, any remaining hooks in the before or after stages MUST NOT be invoked.")
    @SneakyThrows
    @Test
    void error_stops_after() {
        Hook<Boolean> h = mockBooleanHook();
        doThrow(RuntimeException.class).when(h).after(any(), any(), any());
        Hook<Boolean> h2 = mockBooleanHook();

        Client c = getClient(TestEventsProvider.newInitializedTestEventsProvider());

        c.getBooleanDetails("key", false, null, FlagEvaluationOptions.builder()
                .hook(h)
                .hook(h2)
                .build());
        verify(h, times(1)).after(any(), any(), any());
        verify(h2, times(0)).after(any(), any(), any());
    }

    @Specification(number = "4.2.1", text = "hook hints MUST be a structure supports definition of arbitrary properties, with keys of type string, and values of type boolean | string | number | datetime | structure..")
    @Specification(number = "4.5.2", text = "hook hints MUST be passed to each hook.")
    @Specification(number = "4.2.2.1", text = "Condition: Hook hints MUST be immutable.")
    @Specification(number = "4.5.3", text = "The hook MUST NOT alter the hook hints structure.")
    @SneakyThrows
    @Test
    void hook_hints() {
        String hintKey = "My hint key";
        Client client = getClient(null);
        Hook<Boolean> mutatingHook = new BooleanHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, Map<String, Object> hints) {
                assertThatCode(() -> hints.put(hintKey, "changed value")).isInstanceOf(UnsupportedOperationException.class);
                return Optional.empty();
            }

            @Override
            public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, Map<String, Object> hints) {
                assertThatCode(() -> hints.put(hintKey, "changed value")).isInstanceOf(UnsupportedOperationException.class);
            }

            @Override
            public void error(HookContext<Boolean> ctx, Exception error, Map<String, Object> hints) {
                assertThatCode(() -> hints.put(hintKey, "changed value")).isInstanceOf(UnsupportedOperationException.class);
            }

            @Override
            public void finallyAfter(HookContext<Boolean> ctx, Map<String, Object> hints) {
                assertThatCode(() -> hints.put(hintKey, "changed value")).isInstanceOf(UnsupportedOperationException.class);
            }
        };

        Map<String, Object> hh = new HashMap<>();
        hh.put(hintKey, "My hint value");
        hh = Collections.unmodifiableMap(hh);

        client.getBooleanValue("key", false, new ImmutableContext(), FlagEvaluationOptions.builder()
                .hook(mutatingHook)
                .hookHints(hh)
                .build());
    }

    @Specification(number = "4.5.1", text = "Flag evaluation options MAY contain hook hints, a map of data to be provided to hook invocations.")
    @Test
    void missing_hook_hints() {
        FlagEvaluationOptions feo = FlagEvaluationOptions.builder().build();
        assertNotNull(feo.getHookHints());
        assertTrue(feo.getHookHints().isEmpty());
    }

    @Test
    void flag_eval_hook_order() {
        Hook hook = mockBooleanHook();
        FeatureProvider provider = mock(FeatureProvider.class);
        when(provider.getBooleanEvaluation(any(), any(), any()))
                .thenReturn(ProviderEvaluation.<Boolean>builder()
                        .value(true)
                        .build());
        InOrder order = inOrder(hook, provider);

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProviderTestUtils.setFeatureProvider(provider);
        Client client = api.getClient();
        client.getBooleanValue("key", false, new ImmutableContext(),
                FlagEvaluationOptions.builder().hook(hook).build());

        order.verify(hook).before(any(), any());
        order.verify(provider).getBooleanEvaluation(any(), any(), any());
        order.verify(hook).after(any(), any(), any());
        order.verify(hook).finallyAfter(any(), any());
    }

    @Specification(number = "4.4.5", text = "If an error occurs in the before or after hooks, the error hooks MUST be invoked.")
    @Specification(number = "4.4.7", text = "If an error occurs in the before hooks, the default value MUST be returned.")
    @Test
    void error_hooks__before() {
        Hook hook = mockBooleanHook();
        doThrow(RuntimeException.class).when(hook).before(any(), any());
        Client client = getClient(TestEventsProvider.newInitializedTestEventsProvider());
        Boolean value = client.getBooleanValue("key", false, new ImmutableContext(),
                FlagEvaluationOptions.builder().hook(hook).build());
        verify(hook, times(1)).before(any(), any());
        verify(hook, times(1)).error(any(), any(), any());
        assertEquals(false, value, "Falls through to the default.");
    }

    @Specification(number = "4.4.5", text = "If an error occurs in the before or after hooks, the error hooks MUST be invoked.")
    @Test
    void error_hooks__after() {
        Hook hook = mockBooleanHook();
        doThrow(RuntimeException.class).when(hook).after(any(), any(), any());
        Client client = getClient(TestEventsProvider.newInitializedTestEventsProvider());
        client.getBooleanValue("key", false, new ImmutableContext(),
                FlagEvaluationOptions.builder().hook(hook).build());
        verify(hook, times(1)).after(any(), any(), any());
        verify(hook, times(1)).error(any(), any(), any());
    }

    @Test
    void multi_hooks_early_out__before() {
        Hook<Boolean> hook = mockBooleanHook();
        Hook<Boolean> hook2 = mockBooleanHook();
        doThrow(RuntimeException.class).when(hook).before(any(), any());

        Client client = getClient(null);

        client.getBooleanValue("key", false, new ImmutableContext(),
                FlagEvaluationOptions.builder()
                        .hook(hook2)
                        .hook(hook)
                        .build());

        verify(hook, times(1)).before(any(), any());
        verify(hook2, times(0)).before(any(), any());

        verify(hook, times(1)).error(any(), any(), any());
        verify(hook2, times(1)).error(any(), any(), any());
    }

    @Specification(number = "4.1.4", text = "The evaluation context MUST be mutable only within the before hook.")
    @Specification(number = "4.3.4", text = "Any `evaluation context` returned from a `before` hook MUST be passed to subsequent `before` hooks (via `HookContext`).")
    @Test
    void beforeContextUpdated() {
        String targetingKey = "test-key";
        EvaluationContext ctx = new ImmutableContext(targetingKey);
        Hook<Boolean> hook = mockBooleanHook();
        when(hook.before(any(), any())).thenReturn(Optional.of(ctx));
        Hook<Boolean> hook2 = mockBooleanHook();
        when(hook.before(any(), any())).thenReturn(Optional.empty());
        InOrder order = inOrder(hook, hook2);

        Client client = getClient(null);
        client.getBooleanValue("key", false, ctx,
                FlagEvaluationOptions.builder()
                        .hook(hook2)
                        .hook(hook)
                        .build());

        order.verify(hook).before(any(), any());
        ArgumentCaptor<HookContext<Boolean>> captor = ArgumentCaptor.forClass(HookContext.class);
        order.verify(hook2).before(captor.capture(), any());

        HookContext<Boolean> hc = captor.getValue();
        assertEquals(hc.getCtx().getTargetingKey(), targetingKey);

    }

    @Specification(number = "4.3.5", text = "When before hooks have finished executing, any resulting evaluation context MUST be merged with the existing evaluation context.")
    @Test
    void mergeHappensCorrectly() {
        Map<String, Value> attributes = new HashMap<>();
        attributes.put("test", new Value("works"));
        attributes.put("another", new Value("exists"));
        EvaluationContext hookCtx = new ImmutableContext(attributes);


        Map<String, Value> attributes1 = new HashMap<>();
        attributes1.put("something", new Value("here"));
        attributes1.put("test", new Value("broken"));
        EvaluationContext invocationCtx = new ImmutableContext(attributes1);

        Hook<Boolean> hook = mockBooleanHook();
        when(hook.before(any(), any())).thenReturn(Optional.of(hookCtx));

        FeatureProvider provider = mock(FeatureProvider.class);
        when(provider.getBooleanEvaluation(any(), any(), any())).thenReturn(ProviderEvaluation.<Boolean>builder()
                .value(true)
                .build());

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProviderTestUtils.setFeatureProvider(provider);
        Client client = api.getClient();
        client.getBooleanValue("key", false, invocationCtx,
                FlagEvaluationOptions.builder()
                        .hook(hook)
                        .build());

        ArgumentCaptor<ImmutableContext> captor = ArgumentCaptor.forClass(ImmutableContext.class);
        verify(provider).getBooleanEvaluation(any(), any(), captor.capture());
        EvaluationContext ec = captor.getValue();
        assertEquals("works", ec.getValue("test").asString());
        assertEquals("exists", ec.getValue("another").asString());
        assertEquals("here", ec.getValue("something").asString());
    }

    @Specification(number = "4.4.3", text = "If a finally hook abnormally terminates, evaluation MUST proceed, including the execution of any remaining finally hooks.")
    @Test
    void first_finally_broken() {
        Hook hook = mockBooleanHook();
        doThrow(RuntimeException.class).when(hook).before(any(), any());
        doThrow(RuntimeException.class).when(hook).finallyAfter(any(), any());
        Hook hook2 = mockBooleanHook();
        InOrder order = inOrder(hook, hook2);

        Client client = getClient(null);
        client.getBooleanValue("key", false, new ImmutableContext(),
                FlagEvaluationOptions.builder()
                        .hook(hook2)
                        .hook(hook)
                        .build());

        order.verify(hook).before(any(), any());
        order.verify(hook2).finallyAfter(any(), any());
        order.verify(hook).finallyAfter(any(), any());
    }

    @Specification(number = "4.4.4", text = "If an error hook abnormally terminates, evaluation MUST proceed, including the execution of any remaining error hooks.")
    @Test
    void first_error_broken() {
        Hook hook = mockBooleanHook();
        doThrow(RuntimeException.class).when(hook).before(any(), any());
        doThrow(RuntimeException.class).when(hook).error(any(), any(), any());
        Hook hook2 = mockBooleanHook();
        InOrder order = inOrder(hook, hook2);

        Client client = getClient(null);
        client.getBooleanValue("key", false, new ImmutableContext(),
                FlagEvaluationOptions.builder()
                        .hook(hook2)
                        .hook(hook)
                        .build());

        order.verify(hook).before(any(), any());
        order.verify(hook2).error(any(), any(), any());
        order.verify(hook).error(any(), any(), any());
    }

    private Client getClient(FeatureProvider provider) {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        if (provider == null) {
            FeatureProviderTestUtils.setFeatureProvider(TestEventsProvider.newInitializedTestEventsProvider());
        } else {
            FeatureProviderTestUtils.setFeatureProvider(provider);
        }
        return api.getClient();
    }

    @Specification(number = "4.3.1", text = "Hooks MUST specify at least one stage.")
    @Test
    void default_methods_so_impossible() {
    }

    @Specification(number = "4.3.9.1", text = "Instead of finally, finallyAfter SHOULD be used.")
    @SneakyThrows
    @Test
    void doesnt_use_finally() {
        assertThatCode(() -> Hook.class.getMethod("finally", HookContext.class, Map.class))
                .as("Not possible. Finally is a reserved word.")
                .isInstanceOf(NoSuchMethodException.class);

        assertThatCode(() -> Hook.class.getMethod("finallyAfter", HookContext.class, Map.class))
                .doesNotThrowAnyException();
    }

}
