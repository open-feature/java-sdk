package dev.openfeature.sdk.benchmark.state;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.ImmutableMetadata;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.StringHook;
import dev.openfeature.sdk.providers.memory.Flag;
import dev.openfeature.sdk.providers.memory.InMemoryProvider;
import java.util.Map;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

@State(Scope.Benchmark)
public class HooksState {
    public static final String FLAG_KEY = "flag-key";
    public static final String DOMAIN = "jmh-domain";

    public InMemoryProvider provider;
    public Client client;

    @Setup(Level.Trial)
    public void setup() {
        provider = new InMemoryProvider(Map.of(
                FLAG_KEY,
                Flag.builder()
                        .variant("a", "a-value")
                        .variant("b", "b-value")
                        .defaultVariant("b")
                        .flagMetadata(ImmutableMetadata.builder()
                                .addString("meta", "data")
                                .build())
                        .build()));
        OpenFeatureAPI.getInstance().setProviderAndWait(DOMAIN, provider);
        client = OpenFeatureAPI.getInstance().getClient(DOMAIN);
        client.addHooks(new StringHook() {}, new Hook() {});
    }

    @TearDown(Level.Trial)
    public void teardown() {
        OpenFeatureAPI.getInstance().shutdown();
        OpenFeatureAPI.getInstance().clearHooks();
    }
}
