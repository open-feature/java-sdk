package dev.openfeature.sdk.multiProvider;

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
import dev.openfeature.sdk.multiprovider.MultiProvider;
import dev.openfeature.sdk.multiprovider.MultiProviderMetadata;
import dev.openfeature.sdk.multiprovider.Strategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

class MultiProviderTest extends BaseStrategyTest {

    @SneakyThrows
    @Test
    void shouldInitializeSuccessfully() {
        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(mockProvider1);
        providers.add(mockProvider2);
        Strategy strategy = mock(Strategy.class);
        MultiProvider multiProvider = new MultiProvider(providers, strategy);
        multiProvider.initialize(null);

        assertNotNull(multiProvider);
        MultiProviderMetadata metadata = (MultiProviderMetadata) multiProvider.getMetadata();
        Map<String, Metadata> map = metadata.getOriginalMetadata();
        assertEquals(mockMetaData1, map.get(mockProvider1.getMetadata().getName()));
        assertEquals(mockMetaData2, map.get(mockProvider2.getMetadata().getName()));
        assertEquals("multiprovider", multiProvider.getMetadata().getName());
    }

    @SneakyThrows
    @Test
    void shouldHandleInitializationFailure() {
        doThrow(new GeneralError()).when(mockProvider1).initialize(any());
        doThrow(new GeneralError()).when(mockProvider1).shutdown();
        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(mockProvider1);
        providers.add(mockProvider2);
        Strategy strategy = mock(Strategy.class);
        MultiProvider multiProvider = new MultiProvider(providers, strategy);
        assertThrows(ExecutionException.class, () -> multiProvider.initialize(null));
        assertDoesNotThrow(multiProvider::shutdown);
    }

    @Test
    void shouldHandleDuplicateProviderNames() {
        when(mockProvider1.getMetadata()).thenReturn(() -> "provider");
        when(mockProvider2.getMetadata()).thenReturn(() -> "provider");
        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(mockProvider1);
        providers.add(mockProvider2);
        assertDoesNotThrow(() -> new MultiProvider(providers, null).initialize(null));
    }

    @SneakyThrows
    @Test
    void shouldRetrieveCorrectMetadataName() {
        List<FeatureProvider> providers = new ArrayList<>();
        providers.add(mockProvider1);
        Strategy mockStrategy = mock(Strategy.class);
        MultiProvider multiProvider = new MultiProvider(providers, mockStrategy);
        multiProvider.initialize(null);
        assertNotNull(multiProvider);
        MultiProviderMetadata metadata = (MultiProviderMetadata) multiProvider.getMetadata();
        Map<String, Metadata> map = metadata.getOriginalMetadata();
        assertEquals(mockMetaData1, map.get(mockProvider1.getMetadata().getName()));
    }

    @SneakyThrows
    @Test
    void shouldUseDefaultFirstMatchStrategy() {
        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(inMemoryProvider1);
        providers.add(inMemoryProvider2);
        MultiProvider multiProvider = new MultiProvider(providers);
        multiProvider.initialize(null);
        assertEquals(true, multiProvider.getBooleanEvaluation("b1", false, null).getValue());
        assertEquals(1, multiProvider.getIntegerEvaluation("i1", 0, null).getValue());
        assertEquals(1.0, multiProvider.getDoubleEvaluation("d1", 0.0, null).getValue());
        assertEquals("str1", multiProvider.getStringEvaluation("s1", "", null).getValue());
        assertEquals(
                "v1",
                multiProvider.getObjectEvaluation("o1", null, null).getValue().asString());
        assertThrows(FlagNotFoundError.class, () -> multiProvider.getStringEvaluation("non-existing", "", null));
    }

    @SneakyThrows
    @Test
    void shouldWorkWithCustomStrategy() {
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

        List<FeatureProvider> providers = new ArrayList<>(2);
        providers.add(inMemoryProvider1);
        providers.add(inMemoryProvider2);
        MultiProvider multiProvider = new MultiProvider(providers, customStrategy);
        multiProvider.initialize(null);
        EvaluationContext context = new MutableContext().add("provider", "new-provider");
        assertEquals(
                false, multiProvider.getBooleanEvaluation("b1", true, context).getValue());
        assertEquals(true, multiProvider.getBooleanEvaluation("b1", true, null).getValue());
    }

    @SneakyThrows
    @Test
    void shouldSupportAllEvaluationTypes() {
        List<FeatureProvider> providers = new ArrayList<>(1);
        providers.add(inMemoryProvider1);
        MultiProvider multiProvider = new MultiProvider(providers);
        multiProvider.initialize(null);
        assertNotNull(multiProvider.getBooleanEvaluation("b1", false, null));
        assertNotNull(multiProvider.getIntegerEvaluation("i1", 0, null));
        assertNotNull(multiProvider.getDoubleEvaluation("d1", 0.0, null));
        assertNotNull(multiProvider.getStringEvaluation("s1", "", null));
        assertNotNull(multiProvider.getObjectEvaluation("o1", null, null));
    }
}
