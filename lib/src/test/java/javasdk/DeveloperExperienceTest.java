package javasdk;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class DeveloperExperienceTest {
    @Test public void simpleBooleanFlag() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        Boolean retval = client.getBooleanValue("mykey", false);
        assertEquals(false, retval);
    }

    @Test public void clientHooks() {
        Hook<Boolean> exampleHook = Mockito.mock(Hook.class);

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.registerHooks(exampleHook);
        Boolean retval = client.getBooleanValue("mykey", false);
        verify(exampleHook, times(1)).afterAll(any());
        assertEquals(false, retval);
    }

    @Test public void evalHooks() {
        Hook<Boolean> clientHook = Mockito.mock(Hook.class);
        Hook<Boolean> evalHook = Mockito.mock(Hook.class);

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.registerHooks(clientHook);
        Boolean retval = client.getBooleanValue("mykey", false, new EvaluationContext(),
                FlagEvaluationOptions.builder().hook(evalHook).build());
        verify(clientHook, times(1)).afterAll(any());
        verify(evalHook, times(1)).afterAll(any());
        assertEquals(false, retval);
    }
}
