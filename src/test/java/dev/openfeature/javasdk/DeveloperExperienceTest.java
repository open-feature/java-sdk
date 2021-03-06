package dev.openfeature.javasdk;

import dev.openfeature.javasdk.fixtures.HookFixtures;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@SuppressWarnings("unchecked")
class DeveloperExperienceTest implements HookFixtures {
    transient String flagKey = "mykey";
    @Test void simpleBooleanFlag() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        Boolean retval = client.getBooleanValue(flagKey, false);
        assertFalse(retval);
    }

    @Test void clientHooks() {
        Hook<Boolean> exampleHook = mockBooleanHook();

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.addHooks(exampleHook);
        Boolean retval = client.getBooleanValue(flagKey, false);
        verify(exampleHook, times(1)).finallyAfter(any(), any());
        assertFalse(retval);
    }

    @Test void evalHooks() {
        Hook<Boolean> clientHook = mockBooleanHook();
        Hook<Boolean> evalHook = mockBooleanHook();

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.addHooks(clientHook);
        Boolean retval = client.getBooleanValue(flagKey, false, null,
                FlagEvaluationOptions.builder().hook(evalHook).build());
        verify(clientHook, times(1)).finallyAfter(any(), any());
        verify(evalHook, times(1)).finallyAfter(any(), any());
        assertFalse(retval);
    }

    @Test void brokenProvider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new AlwaysBrokenProvider());
        Client client = api.getClient();
        FlagEvaluationDetails<Boolean> retval = client.getBooleanDetails(flagKey, false);
        assertEquals("BORK", retval.getErrorCode());
        assertEquals(Reason.ERROR, retval.getReason());
        assertFalse(retval.getValue());
    }
}
