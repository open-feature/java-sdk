package dev.openfeature.sdk.fixtures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.ProviderState;
import java.io.FileNotFoundException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProviderFixture {

    public static FeatureProvider createMockedProvider() {
        FeatureProvider provider = mock(FeatureProvider.class);
        doReturn(ProviderState.NOT_READY).when(provider).getState();
        return provider;
    }

    public static FeatureProvider createMockedReadyProvider() {
        FeatureProvider provider = mock(FeatureProvider.class);
        doReturn(ProviderState.READY).when(provider).getState();
        return provider;
    }

    public static FeatureProvider createMockedErrorProvider() throws Exception {
        FeatureProvider provider = mock(FeatureProvider.class);
        doReturn(ProviderState.NOT_READY).when(provider).getState();
        doThrow(FileNotFoundException.class).when(provider).initialize(any(), nullable(String.class));
        return provider;
    }
}
