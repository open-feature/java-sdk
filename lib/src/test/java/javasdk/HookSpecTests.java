package javasdk;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class HookSpecTests {
    @AfterEach
    void emptyApiHooks() {
        // it's a singleton. Don't pollute each test.
        OpenFeatureAPI.getInstance().clearHooks();
    }

    @Specification(spec="hooks", number="1.3", text="flag key, flag type, default value properties MUST be immutable. If the language does not support immutability, the hook MUST NOT modify these properties.")
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

    @Specification(spec="hooks", number="1.1", text="Hook context MUST provide: the flag key, flag type, evaluation context, and the default value.")
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
                    .ctx(new EvaluationContext())
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
                    .ctx(new EvaluationContext())
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

    @Specification(spec="hooks", number="1.2", text="Hook context SHOULD provide: provider (instance) and client (instance)")
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

    @Specification(spec="hooks", number="3.2", text="The before stage MUST run before flag evaluation occurs. It accepts a hook context (required) and HookHints (optional) as parameters and returns either a HookContext or nothing.")
    @Test void before_runs_ahead_of_evaluation() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client client = api.getClient();
        Hook<Boolean> evalHook = (Hook<Boolean>) mock(Hook.class);

        client.getBooleanValue("key", false, new EvaluationContext(),
                FlagEvaluationOptions.builder().hook(evalHook).build());

        verify(evalHook, times(1)).before(any());
    }

    @Specification(spec="hooks", number="5.1", text="Flag evalution options MUST contain a list of hooks to evaluate.")
    @Test void feo_has_hook_list() {
        FlagEvaluationOptions feo = FlagEvaluationOptions.builder()
                .build();
        assertNotNull(feo.getHooks());
    }

    @Specification(spec="hooks", number="4.3", text="If an error occurs in the finally hook, it MUST NOT trigger the error hook.")
    @Test void errors_in_finally() {
        Hook<Boolean> h = mock(Hook.class);
        doThrow(RuntimeException.class).when(h).finallyAfter(any());

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider<>());
        Client c= api.getClient();

        assertThrows(RuntimeException.class, () -> c.getBooleanValue("key", false, null, FlagEvaluationOptions.builder().hook(h).build()));

        verify(h, times(1)).finallyAfter(any());
        verify(h, times(0)).error(any(), any());
    }

    @Specification(spec="hooks", number="3.4", text="The error hook MUST run when errors are encountered in the before stage, the after stage or during flag evaluation. It accepts hook context (required), exception for what went wrong (required), and HookHints (optional). It has no return value.")
    @Test void error_hook_run_during_non_finally_stage() {
        final boolean[] error_called = {false};
        Hook h = mock(Hook.class);
        doThrow(RuntimeException.class).when(h).finallyAfter(any());

        verify(h, times(0)).error(any(), any());
    }
    @Specification(spec="hooks", number="4.2", text="Hooks MUST be evaluated in the following order:" +
            "before: API, Client, Invocation" +
            "after: Invocation, Client, API" +
            "error (if applicable): Invocation, Client, API" +
            "finally: Invocation, Client, API")
    @Test void eval_order() {
        List<String> evalOrder = new ArrayList<String>();
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        api.registerHooks(new Hook() {
            @Override
            void before(HookContext ctx) {
                evalOrder.add("api before");
            }

            @Override
            void after(HookContext ctx, FlagEvaluationDetails details) {
                evalOrder.add("api after");
                throw new RuntimeException(); // trigger error flows.
            }

            @Override
            void error(HookContext ctx, Exception error) {
                evalOrder.add("api error");
            }

            @Override
            void finallyAfter(HookContext ctx) {
                evalOrder.add("api finally");
            }
        });

        Client c = api.getClient();
        c.registerHooks(new Hook() {
            @Override
            void before(HookContext ctx) {
                evalOrder.add("client before");
            }

            @Override
            void after(HookContext ctx, FlagEvaluationDetails details) {
                evalOrder.add("client after");
            }

            @Override
            void error(HookContext ctx, Exception error) {
                evalOrder.add("client error");
            }

            @Override
            void finallyAfter(HookContext ctx) {
                evalOrder.add("client finally");
            }
        });

        c.getBooleanValue("key", false, null, FlagEvaluationOptions.builder()
                        .hook(new Hook() {
                            @Override
                            void before(HookContext ctx) {
                                evalOrder.add("invocation before");
                            }

                            @Override
                            void after(HookContext ctx, FlagEvaluationDetails details) {
                                evalOrder.add("invocation after");
                            }

                            @Override
                            void error(HookContext ctx, Exception error) {
                                evalOrder.add("invocation error");
                            }

                            @Override
                            void finallyAfter(HookContext ctx) {
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

    @Specification(spec="hooks", number="1.4", text="The evaluation context MUST be mutable only within the before hook.")
    @Specification(spec="hooks", number="2.1", text="HookHints MUST be a map of objects.")
    @Specification(spec="hooks", number="2.2", text="Condition: HookHints MUST be immutable.")
    @Specification(spec="hooks", number="3.1", text="Hooks MUST specify at least one stage.")
    @Specification(spec="hooks", number="3.3", text="The after stage MUST run after flag evaluation occurs. It accepts a hook context (required), flag evaluation details (required) and HookHints (optional). It has no return value.")
    @Specification(spec="hooks", number="3.5", text="The finally hook MUST run after the before, after, and error stages. It accepts a hook context (required) and HookHints (optional). There is no return value.")
    @Specification(spec="hooks", number="4.1", text="The API, Client and invocation MUST have a method for registering hooks which accepts flag evaluation options")
    @Specification(spec="hooks", number="4.4", text="If an error occurs in the before or after hooks, the error hooks MUST be invoked.")
    @Specification(spec="hooks", number="4.5", text="If an error occurs during the evaluation of before or after hooks, any remaining hooks in the before or after stages MUST NOT be invoked.")
    @Specification(spec="hooks", number="4.6", text="If an error is encountered in the error stage, it MUST NOT be returned to the user.")
    @Specification(spec="hooks", number="5.2", text="Flag evaluation options MAY contain HookHints, a map of data to be provided to hook invocations.")
    @Specification(spec="hooks", number="5.3", text="HookHints MUST be passed to each hook through a parameter. It is merged into the object in the precedence order API -> Client -> Invocation (last wins).")
    @Specification(spec="hooks", number="5.4", text="The hook MUST NOT alter the HookHints object.")
    @Specification(spec="hooks", number="6.1", text="HookHints MUST passed between each hook.")
    void todo() {}

    @SneakyThrows
    @Specification(spec="hooks", number="3.6", text="Condition: If finally is a reserved word in the language, finallyAfter SHOULD be used.")
    @Disabled
    @Test void doesnt_use_finally() {
//        Class [] carr = new Class[1];
//        carr[0] = HookContext.class;
//
//        try {
//            Hook.class.getMethod("finally", carr);
//            fail("Not possible. Finally is a reserved word.");
//        } catch (NoSuchMethodException e) {
//            // expected
//        }

        Hook.class.getMethod("finallyAfter", HookContext.class);

    }


}
