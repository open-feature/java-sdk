package dev.openfeature.sdk.e2e.steps;

import static dev.openfeature.sdk.testutils.TestFlagsUtils.buildFlags;

import dev.openfeature.sdk.e2e.State;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import io.cucumber.java.en.Given;
import java.util.Map;

public class ProviderSteps {
    private final State state;

    public ProviderSteps(State state) {
        this.state = state;
    }

    @Given("a stable provider")
    public void aStableProvider() {
        Map<String, Flag<?>> flags = buildFlags();
        InMemoryProvider provider = new InMemoryProvider(flags);
        state.api.setProviderAndWait(provider);
        state.client = state.api.getClient();
    }
}
