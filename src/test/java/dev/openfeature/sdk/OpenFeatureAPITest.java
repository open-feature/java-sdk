package dev.openfeature.sdk;

import dev.openfeature.sdk.testutils.FeatureProviderTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class OpenFeatureAPITest {

    private static final String CLIENT_NAME = "client name";

    private OpenFeatureAPI api;

    @BeforeEach
    void setupTest() {
        api = OpenFeatureAPI.getInstance();
    }

    @Test
    void namedProviderTest() {
        FeatureProvider provider = new NoOpProvider();
        FeatureProviderTestUtils.setFeatureProvider("namedProviderTest", provider);

        assertThat(provider.getMetadata().getName())
                .isEqualTo(api.getProviderMetadata("namedProviderTest").getName());
    }

    @Test
    void settingDefaultProviderToNullErrors() {
        assertThatCode(() -> api.setProvider(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settingNamedClientProviderToNullErrors() {
        assertThatCode(() -> api.setProvider(CLIENT_NAME, null)).isInstanceOf(IllegalArgumentException.class);
    }
}
