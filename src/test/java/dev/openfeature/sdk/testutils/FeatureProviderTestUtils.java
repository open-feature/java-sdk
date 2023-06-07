package dev.openfeature.sdk.testutils;

import java.time.Duration;
import java.util.function.Function;

import dev.openfeature.sdk.*;
import lombok.experimental.UtilityClass;

import static org.awaitility.Awaitility.await;

@UtilityClass
public class FeatureProviderTestUtils {

    public static void setFeatureProvider(FeatureProvider provider) {
        OpenFeatureAPI.getInstance().setProvider(provider);
        waitForProviderInitializationComplete(OpenFeatureAPI::getProvider, provider);
    }

    private static void waitForProviderInitializationComplete(Function<OpenFeatureAPI, FeatureProvider> extractor, FeatureProvider provider) {
        await()
            .pollDelay(Duration.ofMillis(1))
            .atMost(Duration.ofSeconds(1))
            .until(() -> extractor.apply(OpenFeatureAPI.getInstance()) == provider);
    }

    public static void setFeatureProvider(String namedProvider, FeatureProvider provider) {
        OpenFeatureAPI.getInstance().setProvider(namedProvider, provider);
        waitForProviderInitializationComplete(api -> api.getProvider(namedProvider), provider);
    }
}
