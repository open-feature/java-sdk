package dev.openfeature.sdk;

import dev.openfeature.sdk.fixtures.HookFixtures;
import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;
import dev.openfeature.sdk.testutils.TestEventsProvider;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class DeveloperExperienceTest implements HookFixtures {
    transient String flagKey = "mykey";

    @Test void simpleBooleanFlag() throws Exception {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(TestEventsProvider.initialized());
        Client client = api.getClient();
        Boolean retval = client.getBooleanValue(flagKey, false);
        assertFalse(retval);
    }

    @Test void clientHooks() throws Exception {
        Hook<Boolean> exampleHook = mockBooleanHook();

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(TestEventsProvider.initialized());
        Client client = api.getClient();
        client.addHooks(exampleHook);
        Boolean retval = client.getBooleanValue(flagKey, false);
        verify(exampleHook, times(1)).finallyAfter(any(), any());
        assertFalse(retval);
    }

    @Test void evalHooks() throws Exception {
        Hook<Boolean> clientHook = mockBooleanHook();
        Hook<Boolean> evalHook = mockBooleanHook();

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(TestEventsProvider.initialized());
        Client client = api.getClient();
        client.addHooks(clientHook);
        Boolean retval = client.getBooleanValue(flagKey, false, null,
                FlagEvaluationOptions.builder().hook(evalHook).build());
        verify(clientHook, times(1)).finallyAfter(any(), any());
        verify(evalHook, times(1)).finallyAfter(any(), any());
        assertFalse(retval);
    }

    /**
     * As an application author, you probably know special things about your users. You can communicate these to the
     * provider via {@link MutableContext}
     */
    @Test void providingContext() throws Exception {

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(TestEventsProvider.initialized());
        Client client = api.getClient();
        Map<String, Value> attributes = new HashMap<>();
        List<Value> values = Arrays.asList(new Value(2), new Value(4));
        attributes.put("int-val", new Value(3));
        attributes.put("double-val", new Value(4.0));
        attributes.put("str-val", new Value("works"));
        attributes.put("bool-val", new Value(false));
        attributes.put("value-val", new Value(values));
        EvaluationContext ctx = new ImmutableContext(attributes);
        Boolean retval = client.getBooleanValue(flagKey, false, ctx);
        assertFalse(retval);
    }

    @Test void brokenProvider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        FeatureProviderTestUtils.setFeatureProvider(new AlwaysBrokenProvider());
        Client client = api.getClient();
        FlagEvaluationDetails<Boolean> retval = client.getBooleanDetails(flagKey, false);
        assertEquals(ErrorCode.FLAG_NOT_FOUND, retval.getErrorCode());
        assertEquals(TestConstants.BROKEN_MESSAGE, retval.getErrorMessage());
        assertEquals(Reason.ERROR.toString(), retval.getReason());
        assertFalse(retval.getValue());
    }

    @Test
    void providerLockedPerTransaction() {

        class MutatingHook implements Hook {

            @Override
            // change the provider during a before hook - this should not impact the evaluation in progress
            public Optional before(HookContext ctx, Map hints)  {
                try {
                    FeatureProviderTestUtils.setFeatureProvider(TestEventsProvider.initialized());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return Optional.empty();
            }
        }
        
        final String defaultValue = "string-value";
        final OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        final Client client = api.getClient();
        FeatureProviderTestUtils.setFeatureProvider(new DoSomethingProvider());
        api.addHooks(new MutatingHook());

        // if provider is changed during an evaluation transaction it should proceed with the original provider
        String doSomethingValue = client.getStringValue("val", defaultValue);
        assertEquals(new StringBuilder(defaultValue).reverse().toString(), doSomethingValue);

        api.clearHooks();
        
        // subsequent evaluations should now use new provider set by hook
        String noOpValue = client.getStringValue("val", defaultValue);
        assertEquals(noOpValue, defaultValue);
    }
}
