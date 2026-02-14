package dev.openfeature.sdk.multiprovider;

import static dev.openfeature.sdk.ErrorCode.FLAG_NOT_FOUND;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.openfeature.sdk.ErrorCode;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.Value;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;

public abstract class BaseStrategyTest {

    protected FeatureProvider mockProvider1;
    protected FeatureProvider mockProvider2;
    protected FeatureProvider mockProvider3;

    protected Metadata mockMetaData1;
    protected Metadata mockMetaData2;
    protected Metadata mockMetaData3;

    protected InMemoryProvider inMemoryProvider1;
    protected InMemoryProvider inMemoryProvider2;

    protected List<FeatureProvider> orderedProviders;

    protected EvaluationContext contextWithNewProvider;

    protected static final String FLAG_KEY = "test-flag";
    protected static final String DEFAULT_STRING = "default";
    protected static final boolean DEFAULT_BOOLEAN = false;
    protected static final int DEFAULT_INTEGER = 0;
    protected static final double DEFAULT_DOUBLE = 0.0;

    @BeforeEach
    void setUp() {
        setupMockProviders();
        setupInMemoryProviders();
        setupOrderedProviders();
        setupEvaluationContexts();
    }

    protected void setupMockProviders() {
        mockProvider1 = mock(FeatureProvider.class);
        mockProvider2 = mock(FeatureProvider.class);
        mockProvider3 = mock(FeatureProvider.class);
        mockMetaData1 = mock(Metadata.class);
        mockMetaData2 = mock(Metadata.class);
        mockMetaData3 = mock(Metadata.class);
        when(mockMetaData1.getName()).thenReturn("provider1");
        when(mockMetaData2.getName()).thenReturn("provider2");
        when(mockMetaData3.getName()).thenReturn("provider3");
        when(mockProvider1.getMetadata()).thenReturn(mockMetaData1);
        when(mockProvider2.getMetadata()).thenReturn(mockMetaData2);
        when(mockProvider3.getMetadata()).thenReturn(mockMetaData3);
    }

    protected void setupInMemoryProviders() {
        Map<String, Flag<?>> flags1 = createFlags1();
        Map<String, Flag<?>> flags2 = createFlags2();

        inMemoryProvider1 = new InMemoryProvider(flags1) {
            @Override
            public Metadata getMetadata() {
                return () -> "old-provider";
            }
        };

        inMemoryProvider2 = new InMemoryProvider(flags2) {
            @Override
            public Metadata getMetadata() {
                return () -> "new-provider";
            }
        };
    }

    protected void setupOrderedProviders() {
        orderedProviders = new ArrayList<>(3);
        orderedProviders.add(mockProvider1);
        orderedProviders.add(mockProvider2);
        orderedProviders.add(mockProvider3);
    }

    protected void setupEvaluationContexts() {
        contextWithNewProvider = new MutableContext().add("provider", "new-provider");
    }

    protected Map<String, Flag<?>> createFlags1() {
        Map<String, Flag<?>> flags = new HashMap<>();

        flags.put(
                "b1",
                Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("on")
                        .build());

        flags.put(
                "i1",
                Flag.builder().variant("default", 1).defaultVariant("default").build());

        flags.put(
                "d1",
                Flag.builder().variant("default", 1.0).defaultVariant("default").build());

        flags.put(
                "s1",
                Flag.builder()
                        .variant("default", "str1")
                        .defaultVariant("default")
                        .build());

        flags.put(
                "o1",
                Flag.builder()
                        .variant("default", new Value("v1"))
                        .defaultVariant("default")
                        .build());

        return flags;
    }

    protected Map<String, Flag<?>> createFlags2() {
        Map<String, Flag<?>> flags = new HashMap<>();

        flags.put(
                "b1",
                Flag.builder()
                        .variant("on", true)
                        .variant("off", false)
                        .defaultVariant("off")
                        .build());

        flags.put(
                "i1",
                Flag.builder().variant("default", 2).defaultVariant("default").build());

        flags.put(
                "d1",
                Flag.builder().variant("default", 2.0).defaultVariant("default").build());

        flags.put(
                "s1",
                Flag.builder()
                        .variant("default", "str2")
                        .defaultVariant("default")
                        .build());

        flags.put(
                "o1",
                Flag.builder()
                        .variant("default", new Value("v2"))
                        .defaultVariant("default")
                        .build());

        flags.put(
                "s2",
                Flag.builder()
                        .variant("default", "s2str2")
                        .defaultVariant("default")
                        .build());

        return flags;
    }

    protected <T> ProviderEvaluation<T> createErrorResult(ErrorCode errorCode) {
        ProviderEvaluation<T> result = mock(ProviderEvaluation.class);
        when(result.getErrorCode()).thenReturn(errorCode);
        return result;
    }

    protected void setupProviderFlagNotFound(FeatureProvider provider) {
        ProviderEvaluation<String> stringResult = createErrorResult(FLAG_NOT_FOUND);
        ProviderEvaluation<Boolean> booleanResult = createErrorResult(FLAG_NOT_FOUND);
        ProviderEvaluation<Integer> integerResult = createErrorResult(FLAG_NOT_FOUND);
        ProviderEvaluation<Double> doubleResult = createErrorResult(FLAG_NOT_FOUND);
        ProviderEvaluation<Value> objectResult = createErrorResult(FLAG_NOT_FOUND);

        when(provider.getStringEvaluation(BaseStrategyTest.FLAG_KEY, DEFAULT_STRING, null))
                .thenReturn(stringResult);
        when(provider.getBooleanEvaluation(BaseStrategyTest.FLAG_KEY, DEFAULT_BOOLEAN, null))
                .thenReturn(booleanResult);
        when(provider.getIntegerEvaluation(BaseStrategyTest.FLAG_KEY, DEFAULT_INTEGER, null))
                .thenReturn(integerResult);
        when(provider.getDoubleEvaluation(BaseStrategyTest.FLAG_KEY, DEFAULT_DOUBLE, null))
                .thenReturn(doubleResult);
        when(provider.getObjectEvaluation(BaseStrategyTest.FLAG_KEY, null, null))
                .thenReturn(objectResult);
    }

    protected void setupProviderError(FeatureProvider provider, ErrorCode errorCode) {
        ProviderEvaluation<String> result = createErrorResult(errorCode);
        when(provider.getStringEvaluation(BaseStrategyTest.FLAG_KEY, DEFAULT_STRING, null))
                .thenReturn(result);
    }

    protected void setupProviderSuccess(FeatureProvider provider, String value) {
        ProviderEvaluation<String> result = mock(ProviderEvaluation.class);
        when(result.getErrorCode()).thenReturn(null);
        when(result.getValue()).thenReturn(value);
        when(provider.getStringEvaluation(BaseStrategyTest.FLAG_KEY, DEFAULT_STRING, null))
                .thenReturn(result);
    }
}
