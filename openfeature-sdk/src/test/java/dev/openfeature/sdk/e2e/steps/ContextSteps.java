package dev.openfeature.sdk.e2e.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.openfeature.api.Hook;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.MutableContext;
import dev.openfeature.api.lifecycle.HookContext;
import dev.openfeature.api.types.ImmutableStructure;
import dev.openfeature.api.types.Value;
import dev.openfeature.sdk.ThreadLocalTransactionContextPropagator;
import dev.openfeature.sdk.e2e.ContextStoringProvider;
import dev.openfeature.sdk.e2e.State;
import dev.openfeature.sdk.e2e.Utils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ContextSteps {
    private final State state;

    public ContextSteps(State state) {
        this.state = state;
    }

    @Given("a stable provider with retrievable context is registered")
    public void setup() {
        ContextStoringProvider provider = new ContextStoringProvider();
        state.provider = provider;
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
        state.client = OpenFeatureAPI.getInstance().getClient();
        OpenFeatureAPI.getInstance().setTransactionContextPropagator(new ThreadLocalTransactionContextPropagator());
    }

    @When("A context entry with key {string} and value {string} is added to the {string} level")
    public void aContextWithKeyAndValueIsAddedToTheLevel(String contextKey, String contextValue, String level) {
        addContextEntry(contextKey, contextValue, level);
    }

    private void addContextEntry(String contextKey, String contextValue, String level) {
        Map<String, Value> data = new HashMap<>();
        data.put(contextKey, new Value(contextValue));
        EvaluationContext context = EvaluationContext.immutableOf(data);
        if ("API".equals(level)) {
            OpenFeatureAPI.getInstance().setEvaluationContext(context);
        } else if ("Transaction".equals(level)) {
            OpenFeatureAPI.getInstance().setTransactionContext(context);
        } else if ("Client".equals(level)) {
            state.client.setEvaluationContext(context);
        } else if ("Invocation".equals(level)) {
            state.invocationContext = context;
        } else if ("Before Hooks".equals(level)) {
            state.client.addHooks(new Hook() {
                @Override
                public Optional<EvaluationContext> before(HookContext ctx, Map hints) {
                    return Optional.of(context);
                }
            });
        } else {
            throw new IllegalArgumentException("Unknown level: " + level);
        }
    }

    @When("Some flag was evaluated")
    public void someFlagWasEvaluated() {
        state.evaluation = state.client.getStringDetails("unused", "unused", state.invocationContext);
    }

    @Then("The merged context contains an entry with key {string} and value {string}")
    public void theMergedContextContainsAnEntryWithKeyAndValue(String contextKey, String contextValue) {
        assertInstanceOf(
                ContextStoringProvider.class,
                state.provider,
                "In order to use this step, you need to set a ContextStoringProvider");
        EvaluationContext ctx = ((ContextStoringProvider) state.provider).getEvaluationContext();
        assertNotNull(ctx);
        assertNotNull(ctx.getValue(contextKey));
        assertNotNull(ctx.getValue(contextKey).asString());
        assertEquals(contextValue, ctx.getValue(contextKey).asString());
    }

    @Given("A table with levels of increasing precedence")
    public void aTableWithLevelsOfIncreasingPrecedence(DataTable levelsTable) {
        state.levels = levelsTable.asList();
    }

    @And(
            "Context entries for each level from API level down to the {string} level, with key {string} and value {string}")
    public void contextEntriesForEachLevelFromAPILevelDownToTheLevelWithKeyAndValue(
            String maxLevel, String key, String value) {
        for (String level : state.levels) {
            addContextEntry(key, value, level);
            if (level.equals(maxLevel)) {
                return;
            }
        }
    }

    @Given("a context containing a key {string} with null value")
    public void a_context_containing_a_key_with_null_value(String key) {
        a_context_containing_a_key_with_type_and_with_value(key, "String", null);
    }

    @Given("a context containing a key {string}, with type {string} and with value {string}")
    public void a_context_containing_a_key_with_type_and_with_value(String key, String type, String value) {
        Map<String, Value> map = state.context.asMap();
        map.put(key, Value.objectToValue(Utils.convert(value, type)));
        state.context = new MutableContext(state.context.getTargetingKey(), map);
    }

    @Given("a context containing a targeting key with value {string}")
    public void a_context_containing_a_targeting_key_with_value(String string) {
        state.context.setTargetingKey(string);
    }

    @Given("a context containing a nested property with outer key {string} and inner key {string}, with value {string}")
    public void a_context_containing_a_nested_property_with_outer_key_and_inner_key_with_value(
            String outer, String inner, String value) {
        Map<String, Value> innerMap = new HashMap<>();
        innerMap.put(inner, new Value(value));
        state.context.add(outer, new ImmutableStructure(innerMap));
    }
}
