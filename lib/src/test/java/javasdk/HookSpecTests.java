package javasdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class HookSpecTests {
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

    @Specification(spec="hooks", number="1.4", text="The evaluation context MUST be mutable only within the before hook.")
    @Specification(spec="hooks", number="2.1", text="HookHints MUST be a map of objects.")
    @Specification(spec="hooks", number="2.2", text="Condition: HookHints MUST be immutable.")
    @Specification(spec="hooks", number="3.1", text="Hooks MUST specify at least one stage.")
    @Specification(spec="hooks", number="3.2", text="The before stage MUST run before flag evaluation occurs. It accepts a hook context (required) and HookHints (optional) as parameters and returns either a HookContext or nothing.")
    @Specification(spec="hooks", number="3.3", text="The after stage MUST run after flag evaluation occurs. It accepts a hook context (required), flag evaluation details (required) and HookHints (optional). It has no return value.")
    @Specification(spec="hooks", number="3.4", text="The error hook MUST run when errors are encountered in the before stage, the after stage or during flag evaluation. It accepts hook context (required), exception for what went wrong (required), and HookHints (optional). It has no return value.")
    @Specification(spec="hooks", number="3.5", text="The finally hook MUST run after the before, after, and error stages. It accepts a hook context (required) and HookHints (optional). There is no return value.")
    @Specification(spec="hooks", number="3.6", text="Condition: If finally is a reserved word in the language, finallyAfter SHOULD be used.")
    @Specification(spec="hooks", number="4.1", text="The API, Client and invocation MUST have a method for registering hooks which accepts flag evaluation options")
    @Specification(spec="hooks", number="4.2", text="Hooks MUST be evaluated in the following order:" +
            "before: API, Client, Invocation" +
            "after: Invocation, Client, API" +
            "error (if applicable): Invocation, Client, API" +
            "finally: Invocation, Client, API")
    @Specification(spec="hooks", number="4.3", text=" If an error occurs in the finally hook, it MUST NOT trigger the error hook.")
    @Specification(spec="hooks", number="4.4", text="If an error occurs in the before or after hooks, the error hooks MUST be invoked.")
    @Specification(spec="hooks", number="4.5", text="If an error occurs during the evaluation of before or after hooks, any remaining hooks in the before or after stages MUST NOT be invoked.")
    @Specification(spec="hooks", number="4.6", text="If an error is encountered in the error stage, it MUST NOT be returned to the user.")
    @Specification(spec="hooks", number="5.1", text="Flag evalution options MUST contain a list of hooks to evaluate.")
    @Specification(spec="hooks", number="5.2", text="Flag evaluation options MAY contain HookHints, a map of data to be provided to hook invocations.")
    @Specification(spec="hooks", number="5.3", text="HookHints MUST be passed to each hook through a parameter. It is merged into the object in the precedence order API -> Client -> Invocation (last wins).")
    @Specification(spec="hooks", number="5.4", text="The hook MUST NOT alter the HookHints object.")
    @Specification(spec="hooks", number="6.1", text="HookHints MUST passed between each hook.")
    void todo() {}


}
