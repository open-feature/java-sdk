package dev.openfeature.sdk.multiProvider;

import static dev.openfeature.sdk.ErrorCode.FLAG_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.exceptions.FlagNotFoundError;
import dev.openfeature.sdk.exceptions.GeneralError;
import dev.openfeature.sdk.multiprovider.FirstMatchStrategy;
import dev.openfeature.sdk.multiprovider.FirstSuccessfulStrategy;
import dev.openfeature.sdk.multiprovider.MultiProvider;
import dev.openfeature.sdk.multiprovider.Strategy;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class MultiProviderTest {

    @SneakyThrows
    @Test
    public void testInit() {
        FeatureProvider provider1 = mock(FeatureProvider.class);
        FeatureProvider provider2 = mock(FeatureProvider.class);
        when(provider1.getMetadata()).thenReturn(() -> "provider1");
        when(provider2.getMetadata()).thenReturn(() -> "provider2");

        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(provider1);
        providers.add(provider2);
        Strategy strategy = mock(Strategy.class);
        MultiProvider multiProvider = new MultiProvider(providers, strategy);
        multiProvider.initialize(null);

        assertNotNull(multiProvider);
        assertEquals(
                "{\"originalMetadata\":{\"provider1\":{\"name\":\"provider1\"},"
                        + "\"provider2\":{\"name\":\"provider2\"}},\"name\":\"multiprovider\"}",
                multiProvider.getMetadata().getName());
    }

    @SneakyThrows
    @Test
    public void testInitOneFails() {
        FeatureProvider provider1 = mock(FeatureProvider.class);
        FeatureProvider provider2 = mock(FeatureProvider.class);
        when(provider1.getMetadata()).thenReturn(() -> "provider1");
        when(provider2.getMetadata()).thenReturn(() -> "provider2");
        doThrow(new GeneralError()).when(provider1).initialize(any());
        doThrow(new GeneralError()).when(provider1).shutdown();

        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(provider1);
        providers.add(provider2);
        Strategy strategy = mock(Strategy.class);
        MultiProvider multiProvider = new MultiProvider(providers, strategy);
        assertThrows(ExecutionException.class, () -> multiProvider.initialize(null));
        assertDoesNotThrow(() -> multiProvider.shutdown());
    }

    @Test
    public void testDuplicateProviderNames() {
        FeatureProvider provider1 = mock(FeatureProvider.class);
        FeatureProvider provider2 = mock(FeatureProvider.class);
        when(provider1.getMetadata()).thenReturn(() -> "provider");
        when(provider2.getMetadata()).thenReturn(() -> "provider");

        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(provider1);
        providers.add(provider2);

        assertDoesNotThrow(() -> new MultiProvider(providers, null).initialize(null));
    }

    @SneakyThrows
    @Test
    public void testRetrieveMetadataName() {
        List<FeatureProvider> providers = new ArrayList<>();
        FeatureProvider mockProvider = mock(FeatureProvider.class);
        when(mockProvider.getMetadata()).thenReturn(() -> "MockProvider");
        providers.add(mockProvider);
        Strategy mockStrategy = mock(Strategy.class);
        MultiProvider multiProvider = new MultiProvider(providers, mockStrategy);
        multiProvider.initialize(null);

        assertEquals(
                "{\"originalMetadata\":{\"MockProvider\":{\"name\":\"MockProvider\"}}," + "\"name\":\"multiprovider\"}",
                multiProvider.getMetadata().getName());
    }

    @SneakyThrows
    @Test
    public void testEvaluations() {
        Map<String, Flag<?>> flags1 = new HashMap<>();
        flags1.put(
                "b1",
                Flag.builder()
                        .variant("true", true)
                        .variant("false", false)
                        .defaultVariant("true")
                        .build());
        flags1.put("i1", Flag.builder().variant("v", 1).defaultVariant("v").build());
        flags1.put("d1", Flag.builder().variant("v", 1.0).defaultVariant("v").build());
        flags1.put("s1", Flag.builder().variant("v", "str1").defaultVariant("v").build());
        flags1.put(
                "o1",
                Flag.builder().variant("v", new Value("v1")).defaultVariant("v").build());
        InMemoryProvider provider1 = new InMemoryProvider(flags1) {
            public Metadata getMetadata() {
                return () -> "old-provider";
            }
        };
        Map<String, Flag<?>> flags2 = new HashMap<>();
        flags2.put(
                "b1",
                Flag.builder()
                        .variant("true", true)
                        .variant("false", false)
                        .defaultVariant("false")
                        .build());
        flags2.put("i1", Flag.builder().variant("v", 2).defaultVariant("v").build());
        flags2.put("d1", Flag.builder().variant("v", 2.0).defaultVariant("v").build());
        flags2.put("s1", Flag.builder().variant("v", "str2").defaultVariant("v").build());
        flags2.put(
                "o1",
                Flag.builder().variant("v", new Value("v2")).defaultVariant("v").build());

        flags2.put(
                "s2", Flag.builder().variant("v", "s2str2").defaultVariant("v").build());
        InMemoryProvider provider2 = new InMemoryProvider(flags2) {
            public Metadata getMetadata() {
                return () -> "new-provider";
            }
        };
        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(provider1);
        providers.add(provider2);
        MultiProvider multiProvider = new MultiProvider(providers);
        multiProvider.initialize(null);

        assertEquals(true, multiProvider.getBooleanEvaluation("b1", false, null).getValue());
        assertEquals(1, multiProvider.getIntegerEvaluation("i1", 0, null).getValue());
        assertEquals(1.0, multiProvider.getDoubleEvaluation("d1", 0.0, null).getValue());
        assertEquals("str1", multiProvider.getStringEvaluation("s1", "", null).getValue());
        assertEquals(
                "v1",
                multiProvider.getObjectEvaluation("o1", null, null).getValue().asString());

        assertEquals("s2str2", multiProvider.getStringEvaluation("s2", "", null).getValue());
        MultiProvider finalMultiProvider1 = multiProvider;
        assertThrows(FlagNotFoundError.class, () -> finalMultiProvider1.getStringEvaluation("non-existing", "", null));

        multiProvider.shutdown();
        multiProvider = new MultiProvider(providers, new FirstSuccessfulStrategy());
        multiProvider.initialize(null);

        assertEquals(true, multiProvider.getBooleanEvaluation("b1", false, null).getValue());
        assertEquals(1, multiProvider.getIntegerEvaluation("i1", 0, null).getValue());
        assertEquals(1.0, multiProvider.getDoubleEvaluation("d1", 0.0, null).getValue());
        assertEquals("str1", multiProvider.getStringEvaluation("s1", "", null).getValue());
        assertEquals(
                "v1",
                multiProvider.getObjectEvaluation("o1", null, null).getValue().asString());

        assertEquals("s2str2", multiProvider.getStringEvaluation("s2", "", null).getValue());
        MultiProvider finalMultiProvider2 = multiProvider;
        assertThrows(GeneralError.class, () -> finalMultiProvider2.getStringEvaluation("non-existing", "", null));

        multiProvider.shutdown();
        Strategy customStrategy = new Strategy() {
            final FirstMatchStrategy fallbackStrategy = new FirstMatchStrategy();

            @Override
            public <T> ProviderEvaluation<T> evaluate(
                    Map<String, FeatureProvider> providers,
                    String key,
                    T defaultValue,
                    EvaluationContext ctx,
                    Function<FeatureProvider, ProviderEvaluation<T>> providerFunction) {
                Value contextProvider = null;
                if (ctx != null) {
                    contextProvider = ctx.getValue("provider");
                }
                if (contextProvider != null && "new-provider".equals(contextProvider.asString())) {
                    return providerFunction.apply(providers.get("new-provider"));
                }
                return fallbackStrategy.evaluate(providers, key, defaultValue, ctx, providerFunction);
            }
        };
        multiProvider = new MultiProvider(providers, customStrategy);
        multiProvider.initialize(null);

        EvaluationContext context = new MutableContext().add("provider", "new-provider");
        assertEquals(
                false, multiProvider.getBooleanEvaluation("b1", true, context).getValue());
        assertEquals(true, multiProvider.getBooleanEvaluation("b1", true, null).getValue());
    }

    @Test
    public void testFirstMatchStrategyErrorCode() {
        FeatureProvider provider1 = mock(FeatureProvider.class);
        FeatureProvider provider2 = mock(FeatureProvider.class);
        FeatureProvider provider3 = mock(FeatureProvider.class);

        when(provider1.getMetadata()).thenReturn(() -> "provider1");
        when(provider2.getMetadata()).thenReturn(() -> "provider2");
        when(provider3.getMetadata()).thenReturn(() -> "provider3");

        ProviderEvaluation<String> flagNotFoundResult = mock(ProviderEvaluation.class);
        when(flagNotFoundResult.getErrorCode()).thenReturn(FLAG_NOT_FOUND);

        ProviderEvaluation<String> successResult = mock(ProviderEvaluation.class);
        when(successResult.getErrorCode()).thenReturn(null);
        when(successResult.getValue()).thenReturn("success");

        when(provider1.getStringEvaluation("test", "default", null)).thenReturn(flagNotFoundResult);
        when(provider2.getStringEvaluation("test", "default", null)).thenReturn(successResult);

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", provider1);
        providers.put("provider2", provider2);
        providers.put("provider3", provider3);
        FirstMatchStrategy strategy = new FirstMatchStrategy();
        ProviderEvaluation<String> result = strategy.evaluate(
                providers, "test", "default", null, p -> p.getStringEvaluation("test", "default", null));

        assertEquals("success", result.getValue());
    }

    @Test
    public void testFirstSuccessfulStrategyErrorCode() {
        FeatureProvider provider1 = mock(FeatureProvider.class);
        FeatureProvider provider2 = mock(FeatureProvider.class);
        when(provider1.getMetadata()).thenReturn(() -> "provider1");
        when(provider2.getMetadata()).thenReturn(() -> "provider2");

        ProviderEvaluation<String> flagNotFoundResult = mock(ProviderEvaluation.class);
        when(flagNotFoundResult.getErrorCode()).thenReturn(FLAG_NOT_FOUND);

        ProviderEvaluation<String> successResult = mock(ProviderEvaluation.class);
        when(successResult.getErrorCode()).thenReturn(null);
        when(successResult.getValue()).thenReturn("success");

        when(provider1.getStringEvaluation("test", "default", null)).thenReturn(flagNotFoundResult);
        when(provider2.getStringEvaluation("test", "default", null)).thenReturn(successResult);

        Map<String, FeatureProvider> providers = new LinkedHashMap<>();
        providers.put("provider1", provider1);
        providers.put("provider2", provider2);
        FirstSuccessfulStrategy strategy = new FirstSuccessfulStrategy();
        ProviderEvaluation<String> result = strategy.evaluate(
                providers, "test", "default", null, p -> p.getStringEvaluation("test", "default", null));

        assertEquals("success", result.getValue());
    }
}
