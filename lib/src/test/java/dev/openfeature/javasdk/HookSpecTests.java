package dev.openfeature.javasdk;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HookSpecTests {
    @AfterEach
    void emptyApiHooks() {
        // it's a singleton. Don't pollute each test.
        OpenFeatureAPI.getInstance().clearHooks();
    }

    @Specification(number="4.1.3", text="The flag key, flag type, and default value properties MUST be immutable. If the language does not support immutability, the hook MUST NOT modify these properties.")
    @Test void immutableValues() {
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

    @Specification(number="4.1.1", text="Hook context MUST provide: the flag key, flag value type, evaluation context, and the default value.")
    @Test void nullish_properties_on_hookcontext() {
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
                    .ctx(new EvaluationContext())
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
                    .ctx(new EvaluationContext())
                    .defaultValue(1)
                    .build();
        } catch (NullPointerException e) {
            fail("NPE after we provided all relevant info");
        }

    }

    @Test void optional_properties() {
        // don't specify
        HookContext.<Integer>builder()
                .flagKey("key")
                .type(FlagValueType.INTEGER)
                .ctx(new EvaluationContext())
                .defaultValue(1)
                .build();

        // add optional provider
        HookContext.<Integer>builder()
                .flagKey("key")
                .type(FlagValueType.INTEGER)
                .ctx(new EvaluationContext())
                .provider(new NoOpProvider())
                .defaultValue(1)
                .build();

        // add optional client
        HookContext.<Integer>builder()
                .flagKey("key")
                .type(FlagValueType.INTEGER)
                .ctx(new EvaluationContext())
                .defaultValue(1)
                .client(OpenFeatureAPI.getInstance().getClient())
                .build();
    }

    @Test void before_runs_ahead_of_evaluation() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client client = api.getClient();
        Hook<Boolean> evalHook = (Hook<Boolean>) mock(Hook.class);

        client.getBooleanValue("key", false, new EvaluationContext(),
                FlagEvaluationOptions.builder().hook(evalHook).build());

        verify(evalHook, times(1)).before(any(), any());
    }

    @Test void feo_has_hook_list() {
        FlagEvaluationOptions feo = FlagEvaluationOptions.builder()
                .build();
        assertNotNull(feo.getHooks());
    }

    @Test void errors_in_finally() {
        Hook<Boolean> h = mock(Hook.class);
        doThrow(RuntimeException.class).when(h).finallyAfter(any(), any());

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client c= api.getClient();

        assertThrows(RuntimeException.class, () -> c.getBooleanValue("key", false, null, FlagEvaluationOptions.builder().hook(h).build()));

        verify(h, times(1)).finallyAfter(any(), any());
        verify(h, times(0)).error(any(), any(), any());
    }

    @Test void error_hook_run_during_non_finally_stage() {
        final boolean[] error_called = {false};
        Hook h = mock(Hook.class);
        doThrow(RuntimeException.class).when(h).finallyAfter(any(), any());

        verify(h, times(0)).error(any(), any(), any());
    }

    @Specification(number="4.4.1", text="The API, Client and invocation MUST have a method for registering hooks which accepts flag evaluation options")
    @Test void hook_eval_order() {
        List<String> evalOrder = new ArrayList<String>();
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        api.addHooks(new Hook<Boolean>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                evalOrder.add("api before");
                return null;
            }

            @Override
            public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, ImmutableMap<String, Object> hints) {
                evalOrder.add("api after");
                throw new RuntimeException(); // trigger error flows.
            }

            @Override
            public void error(HookContext<Boolean> ctx, Exception error, ImmutableMap<String, Object> hints) {
                evalOrder.add("api error");
            }

            @Override
            public void finallyAfter(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                evalOrder.add("api finally");
            }
        });

        Client c = api.getClient();
        c.addHooks(new Hook<Boolean>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                evalOrder.add("client before");
                return null;
            }

            @Override
            public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, ImmutableMap<String, Object> hints) {
                evalOrder.add("client after");
            }

            @Override
            public void error(HookContext<Boolean> ctx, Exception error, ImmutableMap<String, Object> hints) {
                evalOrder.add("client error");
            }

            @Override
            public void finallyAfter(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                evalOrder.add("client finally");
            }
        });

        c.getBooleanValue("key", false, null, FlagEvaluationOptions.builder()
                        .hook(new Hook<Boolean>() {
                            @Override
                            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                                evalOrder.add("invocation before");
                                return null;
                            }

                            @Override
                            public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, ImmutableMap<String, Object> hints) {
                                evalOrder.add("invocation after");
                            }

                            @Override
                            public void error(HookContext<Boolean> ctx, Exception error, ImmutableMap<String, Object> hints) {
                                evalOrder.add("invocation error");
                            }

                            @Override
                            public void finallyAfter(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                                evalOrder.add("invocation finally");
                            }
                        })
                .build());

        ArrayList<String> expectedOrder = new ArrayList<String>();
        expectedOrder.addAll(Arrays.asList(
                "api before", "client before", "invocation before",
                "invocation after", "client after", "api after",
                "invocation error", "client error", "api error",
                "invocation finally", "client finally", "api finally"));
        assertEquals(expectedOrder, evalOrder);
    }

    @Specification(number="4.4.6", text="If an error occurs during the evaluation of before or after hooks, any remaining hooks in the before or after stages MUST NOT be invoked.")
    @Disabled("Not actually sure what 'returned to the user' means in this context. There is no exception information returned.")
    @Test void error_in_error_stage() {
        Hook<Boolean> h = mock(Hook.class);
        doThrow(RuntimeException.class).when(h).error(any(), any(), any());

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client c = api.getClient();

        FlagEvaluationDetails<Boolean> details = c.getBooleanDetails("key", false, null, FlagEvaluationOptions.builder().hook(h).build());
    }


    @Test void hook_hints() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        Hook<Boolean> mutatingHook = new Hook<Boolean>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                assertTrue(hints instanceof ImmutableMap);
                return null;
            }

            @Override
            public void after(HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, ImmutableMap<String, Object> hints) {
                assertTrue(hints instanceof ImmutableMap);
            }

            @Override
            public void error(HookContext<Boolean> ctx, Exception error, ImmutableMap<String, Object> hints) {
                assertTrue(hints instanceof ImmutableMap);
            }

            @Override
            public void finallyAfter(HookContext<Boolean> ctx, ImmutableMap<String, Object> hints) {
                assertTrue(hints instanceof ImmutableMap);
            }
        };

        ImmutableMap<String, Object> hh = ImmutableMap.of("My hint key", "My hint value");

        client.getBooleanValue("key", false, new EvaluationContext(), FlagEvaluationOptions.builder()
                .hook(mutatingHook)
                .hookHints(hh)
                .build());
    }

    @Test void missing_hook_hints() {
        FlagEvaluationOptions feo = FlagEvaluationOptions.builder().build();
        assertNotNull(feo.getHookHints());
        assertTrue(feo.getHookHints().isEmpty());
    }

    @Test void flag_eval_hook_order() {
        Hook hook = mock(Hook.class);
        FeatureProvider provider = mock(FeatureProvider.class);
        when(provider.getBooleanEvaluation(any(), any(), any(), any()))
                .thenReturn(ProviderEvaluation.<Boolean>builder()
                        .value(true)
                        .build());
        InOrder order = inOrder(hook, provider);

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(provider);
        Client client = api.getClient();
        client.getBooleanValue("key", false, new EvaluationContext(),
                FlagEvaluationOptions.builder().hook(hook).build());

        order.verify(hook).before(any(),any());
        order.verify(provider).getBooleanEvaluation(any(), any(), any(), any());
        order.verify(hook).after(any(),any(),any());
        order.verify(hook).finallyAfter(any(),any());
    }

    @Test void error_hooks() {
        Hook hook = mock(Hook.class);
        doThrow(RuntimeException.class).when(hook).before(any(), any());
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.getBooleanValue("key", false, new EvaluationContext(),
                FlagEvaluationOptions.builder().hook(hook).build());
        verify(hook, times(1)).before(any(), any());
        verify(hook, times(1)).error(any(), any(), any());
    }

    @Test void multi_hooks_early_out__before() {
        Hook hook = mock(Hook.class);
        Hook hook2 = mock(Hook.class);
        doThrow(RuntimeException.class).when(hook).before(any(), any());

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();

        client.getBooleanValue("key", false, new EvaluationContext(),
                FlagEvaluationOptions.builder()
                        .hook(hook2)
                        .hook(hook)
                        .build());

        verify(hook, times(1)).before(any(), any());
        verify(hook2, times(0)).before(any(), any());

        verify(hook, times(1)).error(any(), any(), any());
        verify(hook2, times(1)).error(any(), any(), any());
    }

    @Test void beforeContextUpdated() {
        EvaluationContext ctx = new EvaluationContext();
        Hook hook = mock(Hook.class);
        when(hook.before(any(), any())).thenReturn(Optional.of(ctx));
        Hook hook2 = mock(Hook.class);
        when(hook.before(any(), any())).thenReturn(Optional.empty());
        InOrder order = inOrder(hook, hook2);



        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.getBooleanValue("key", false, new EvaluationContext(),
                FlagEvaluationOptions.builder()
                        .hook(hook2)
                        .hook(hook)
                        .build());

        order.verify(hook).before(any(), any());
        ArgumentCaptor<HookContext> captor = ArgumentCaptor.forClass(HookContext.class);
        order.verify(hook2).before(captor.capture(), any());

        HookContext hc = captor.getValue();
        assertEquals(hc.getCtx(), ctx);

    }
    @Test void mergeHappensCorrectly() {
        EvaluationContext hookCtx = new EvaluationContext();
        hookCtx.addStringAttribute("test", "broken");
        hookCtx.addStringAttribute("another", "exists");

        EvaluationContext invocationCtx = new EvaluationContext();
        invocationCtx.addStringAttribute("test", "works");

        Hook hook = mock(Hook.class);
        when(hook.before(any(), any())).thenReturn(Optional.of(hookCtx));

        FeatureProvider provider = mock(FeatureProvider.class);
        when(provider.getBooleanEvaluation(any(),any(),any(),any())).thenReturn(ProviderEvaluation.<Boolean>builder()
                        .value(true)
                .build());

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(provider);
        Client client = api.getClient();
        client.getBooleanValue("key", false, invocationCtx,
                FlagEvaluationOptions.builder()
                        .hook(hook)
                        .build());

        ArgumentCaptor<EvaluationContext> captor = ArgumentCaptor.forClass(EvaluationContext.class);
        verify(provider).getBooleanEvaluation(any(), any(), captor.capture(), any());
        EvaluationContext ec = captor.getValue();
        assertEquals("works", ec.getStringAttribute("test"));
        assertEquals("exists", ec.getStringAttribute("another"));
    }


    @Specification(number="4.1.2", text="The hook context SHOULD provide: access to the client metadata and the provider metadata fields.")
    @Specification(number="4.1.4", text="The evaluation context MUST be mutable only within the before hook.")
    @Specification(number="4.2.1", text="hook hints MUST be a structure supports definition of arbitrary properties, with keys of type string, and values of type boolean | string | number | datetime | structure..")
    @Specification(number="4.2.2.1", text="Condition: Hook hints MUST be immutable.")
    @Specification(number="4.2.2.2", text="Condition: The client metadata field in the hook context MUST be immutable.")
    @Specification(number="4.2.2.3", text="Condition: The provider metadata field in the hook context MUST be immutable.")
    @Specification(number="4.3.1", text="Hooks MUST specify at least one stage.")
    @Specification(number="4.3.2", text="The before stage MUST run before flag resolution occurs. It accepts a hook context (required) and hook hints (optional) as parameters and returns either an evaluation context or nothing.")
    @Specification(number="4.3.3", text="Any evaluation context returned from a before hook MUST be passed to subsequent before hooks (via HookContext).")
    @Specification(number="4.3.4", text="When before hooks have finished executing, any resulting evaluation context MUST be merged with the invocation evaluation context with the invocation evaluation context taking precedence in the case of any conflicts.")
    @Specification(number="4.3.5", text="The after stage MUST run after flag resolution occurs. It accepts a hook context (required), flag evaluation details (required) and hook hints (optional). It has no return value.")
    @Specification(number="4.3.6", text="The error hook MUST run when errors are encountered in the before stage, the after stage or during flag resolution. It accepts hook context (required), exception representing what went wrong (required), and hook hints (optional). It has no return value.")
    @Specification(number="4.3.7", text="The finally hook MUST run after the before, after, and error stages. It accepts a hook context (required) and hook hints (optional). There is no return value.")
    @Specification(number="4.3.8.1", text="Instead of finally, finallyAfter SHOULD be used.")
    @Specification(number="4.4.2", text="Hooks MUST be evaluated in the following order:  - before: API, Client, Invocation - after: Invocation, Client, API - error (if applicable): Invocation, Client, API - finally: Invocation, Client, API")
    @Specification(number="4.4.3", text="If a finally hook abnormally terminates, evaluation MUST proceed, including the execution of any remaining finally hooks.")
    @Specification(number="4.4.4", text="If an error hook abnormally terminates, evaluation MUST proceed, including the execution of any remaining error hooks.")
    @Specification(number="4.4.5", text="If an error occurs in the before or after hooks, the error hooks MUST be invoked.")
    @Specification(number="4.5.1", text="Flag evaluation options MAY contain hook hints, a map of data to be provided to hook invocations.")
    @Specification(number="4.5.2", text="hook hints MUST be passed to each hook.")
    @Specification(number="4.5.3", text="The hook MUST NOT alter the hook hints structure.")
    @Test @Disabled void todo() {}

    @SneakyThrows
    @Test void doesnt_use_finally() {
        try {
            Hook.class.getMethod("finally", HookContext.class, ImmutableMap.class);
            fail("Not possible. Finally is a reserved word.");
        } catch (NoSuchMethodException e) {
            // expected
        }

        Hook.class.getMethod("finallyAfter", HookContext.class, ImmutableMap.class);
    }
}
