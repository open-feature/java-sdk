package dev.openfeature.sdk;

import org.junit.jupiter.api.Test;
import dev.openfeature.contrib.providers.flagd.*;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

class NoBreakingChangesTest {
    @Test
    void noBreakingChanges() throws URISyntaxException {
        var testProvider = new FlagdProvider(
                FlagdOptions.builder()
                        .resolverType(Config.Resolver.FILE)
                        .offlineFlagSourcePath(NoBreakingChangesTest.class.getResource("/testFlags.json").getPath().replaceFirst("/", ""))
                        .build()
        );
        var api = new OpenFeatureAPI();
        api.setProviderAndWait(testProvider);

        var client = api.getClient();
        var flagFound =client.getBooleanDetails("basic-boolean", false);
        assertThat(flagFound).isNotNull();
        assertThat(flagFound.getValue()).isTrue();
        assertThat(flagFound.getVariant()).isEqualTo("true");
        assertThat(flagFound.getReason()).isEqualTo(Reason.STATIC.toString());

        var flagNotFound =client.getStringDetails("unknown", "asd");
        assertThat(flagNotFound).isNotNull();
        assertThat(flagNotFound.getValue()).isEqualTo("asd");
        assertThat(flagNotFound.getVariant()).isNull();
        assertThat(flagNotFound.getReason()).isEqualTo(Reason.ERROR.toString());
        assertThat(flagNotFound.getErrorCode()).isEqualTo(ErrorCode.FLAG_NOT_FOUND);

        testProvider.shutdown();
        api.shutdown();
    }
}
