package javasdk;

import javasdk.exceptions.ParseError;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SuppressWarnings("unchecked")
class DeveloperExperienceTest {
    transient String flagKey = "mykey";
    @Test void simpleBooleanFlag() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        Boolean retval = client.getBooleanValue(flagKey, false);
        assertFalse(retval);
    }

    @Test void clientHooks() {
        Hook<Boolean> exampleHook = (Hook<Boolean>) Mockito.mock(Hook.class);

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.registerHooks(exampleHook);
        Boolean retval = client.getBooleanValue(flagKey, false);
        verify(exampleHook, times(1)).afterAll(any());
        assertFalse(retval);
    }

    @Test void evalHooks() {
        Hook<Boolean> clientHook = (Hook<Boolean>) Mockito.mock(Hook.class);
        Hook<Boolean> evalHook = (Hook<Boolean>) Mockito.mock(Hook.class);

        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new NoOpProvider());
        Client client = api.getClient();
        client.registerHooks(clientHook);
        Boolean retval = client.getBooleanValue(flagKey, false, new EvaluationContext(),
                FlagEvaluationOptions.builder().hook(evalHook).build());
        verify(clientHook, times(1)).afterAll(any());
        verify(evalHook, times(1)).afterAll(any());
        assertFalse(retval);
    }

    @Test void brokenProvider() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setProvider(new FeatureProvider() {
            @Override
            public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {
                throw new ParseError();
            }
            @Override public String getName() { return null; }
            @Override public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) { return null; }
            @Override public ProviderEvaluation<Long> getNumberEvaluation(String key, Long defaultValue, EvaluationContext ctx, FlagEvaluationOptions options) {return null;}
        });
        Client client = api.getClient();
        FlagEvaluationDetails<Boolean> retval = client.getBooleanDetails(flagKey, false);
        assertEquals(ErrorCode.PARSE_ERROR, retval.getErrorCode());
        assertEquals(Reason.ERROR, retval.getReason());
        assertFalse(retval.getValue());
    }
}
