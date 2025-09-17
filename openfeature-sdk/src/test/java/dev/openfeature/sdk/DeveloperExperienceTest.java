package dev.openfeature.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dev.openfeature.api.Client;
import dev.openfeature.api.ErrorCode;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.api.FlagEvaluationOptions;
import dev.openfeature.api.Hook;
import dev.openfeature.api.HookContext;
import dev.openfeature.api.MutableContext;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.ProviderEventDetails;
import dev.openfeature.api.ProviderState;
import dev.openfeature.api.Reason;
import dev.openfeature.api.Value;
import dev.openfeature.sdk.fixtures.HookFixtures;
import dev.openfeature.sdk.testutils.TestEventsProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeveloperExperienceTest implements HookFixtures {
    transient String flagKey = "mykey";
    private OpenFeatureAPI api;

    @BeforeEach
    public void setUp() throws Exception {
        api = new DefaultOpenFeatureAPI();
    }

    @Test
    void simpleBooleanFlag() {
        api.setProviderAndWait(new TestEventsProvider());
        Client client = api.getClient();
        Boolean retval = client.getBooleanValue(flagKey, false);
        assertFalse(retval);
    }

    @Test
    void clientHooks() {
        Hook<Boolean> exampleHook = mockBooleanHook();

        api.setProviderAndWait(new TestEventsProvider());
        Client client = api.getClient();
        client.addHooks(exampleHook);
        Boolean retval = client.getBooleanValue(flagKey, false);
        verify(exampleHook, times(1)).finallyAfter(any(), any(), any());
        assertFalse(retval);
    }

    @Test
    void evalHooks() {
        Hook<Boolean> clientHook = mockBooleanHook();
        Hook<Boolean> evalHook = mockBooleanHook();

        api.setProviderAndWait(new TestEventsProvider());
        Client client = api.getClient();
        client.addHooks(clientHook);
        Boolean retval = client.getBooleanValue(
                flagKey,
                false,
                null,
                FlagEvaluationOptions.builder().hook(evalHook).build());
        verify(clientHook, times(1)).finallyAfter(any(), any(), any());
        verify(evalHook, times(1)).finallyAfter(any(), any(), any());
        assertFalse(retval);
    }

    /**
     * As an application author, you probably know special things about your users. You can communicate these to the
     * provider via {@link MutableContext}
     */
    @Test
    void providingContext() {

        api.setProviderAndWait(new TestEventsProvider());
        Client client = api.getClient();
        Map<String, Value> attributes = new HashMap<>();
        List<Value> values = Arrays.asList(new Value(2), new Value(4));
        attributes.put("int-val", new Value(3));
        attributes.put("double-val", new Value(4.0));
        attributes.put("str-val", new Value("works"));
        attributes.put("bool-val", new Value(false));
        attributes.put("value-val", new Value(values));
        EvaluationContext ctx = EvaluationContext.immutableOf(attributes);
        Boolean retval = client.getBooleanValue(flagKey, false, ctx);
        assertFalse(retval);
    }

    @Test
    void brokenProvider() {
        api.setProviderAndWait(new AlwaysBrokenWithExceptionProvider());
        Client client = api.getClient();
        FlagEvaluationDetails<Boolean> retval = client.getBooleanDetails(flagKey, false);
        assertEquals(ErrorCode.FLAG_NOT_FOUND, retval.getErrorCode());
        assertEquals(TestConstants.BROKEN_MESSAGE, retval.getErrorMessage());
        assertEquals(Reason.ERROR.toString(), retval.getReason());
        assertFalse(retval.getValue());
    }

    @Test
    void providerLockedPerTransaction() {

        final String defaultValue = "string-value";
        final OpenFeatureAPI api = new DefaultOpenFeatureAPI();

        class MutatingHook implements Hook {

            @Override
            // change the provider during a before hook - this should not impact the evaluation in progress
            public Optional before(HookContext ctx, Map hints) {
                try {

                    api.setProviderAndWait(TestEventsProvider.newInitializedTestEventsProvider());

                    return Optional.empty();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        final Client client = api.getClient();
        api.setProviderAndWait(new DoSomethingProvider());
        api.addHooks(new MutatingHook());

        // if provider is changed during an evaluation transaction it should proceed with the original provider
        String doSomethingValue = client.getStringValue("val", defaultValue);
        assertEquals(new StringBuilder(defaultValue).reverse().toString(), doSomethingValue);

        api.clearHooks();

        // subsequent evaluations should now use new provider set by hook
        String noOpValue = client.getStringValue("val", defaultValue);
        assertEquals(noOpValue, defaultValue);
    }

    @Test
    void setProviderAndWaitShouldPutTheProviderInReadyState() {
        String domain = "domain";
        api.setProviderAndWait(domain, new TestEventsProvider());
        Client client = api.getClient(domain);
        assertThat(client.getProviderState()).isEqualTo(ProviderState.READY);
    }

    @Specification(
            number = "5.3.5",
            text =
                    "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @Test
    void shouldPutTheProviderInStateErrorAfterEmittingErrorEvent() {
        String domain = "domain";
        TestEventsProvider provider = new TestEventsProvider();
        api.setProviderAndWait(domain, provider);
        Client client = api.getClient(domain);
        assertThat(client.getProviderState()).isEqualTo(ProviderState.READY);
        provider.emitProviderError(ProviderEventDetails.builder().build()).await();
        assertThat(client.getProviderState()).isEqualTo(ProviderState.ERROR);
    }

    @Specification(
            number = "5.3.5",
            text =
                    "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @Test
    void shouldPutTheProviderInStateStaleAfterEmittingStaleEvent() {
        String domain = "domain";
        TestEventsProvider provider = new TestEventsProvider();
        api.setProviderAndWait(domain, provider);
        Client client = api.getClient(domain);
        assertThat(client.getProviderState()).isEqualTo(ProviderState.READY);
        provider.emitProviderStale(ProviderEventDetails.builder().build()).await();
        assertThat(client.getProviderState()).isEqualTo(ProviderState.STALE);
    }

    @Specification(
            number = "5.3.5",
            text =
                    "If the provider emits an event, the value of the client's provider status MUST be updated accordingly.")
    @Test
    void shouldPutTheProviderInStateReadyAfterEmittingReadyEvent() {
        String domain = "domain";
        TestEventsProvider provider = new TestEventsProvider();
        api.setProviderAndWait(domain, provider);
        Client client = api.getClient(domain);
        assertThat(client.getProviderState()).isEqualTo(ProviderState.READY);
        provider.emitProviderStale(ProviderEventDetails.builder().build()).await();
        assertThat(client.getProviderState()).isEqualTo(ProviderState.STALE);
        provider.emitProviderReady(ProviderEventDetails.builder().build()).await();
        assertThat(client.getProviderState()).isEqualTo(ProviderState.READY);
    }
}
