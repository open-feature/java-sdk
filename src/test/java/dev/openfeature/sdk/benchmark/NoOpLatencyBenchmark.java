package dev.openfeature.sdk.benchmark;

import dev.openfeature.sdk.BooleanHook;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.FlagEvaluationOptions;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.MutableContext;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.ThreadLocalTransactionContextPropagator;
import dev.openfeature.sdk.Value;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Measures evaluation latency with no provider set (evaluations short-circuit with PROVIDER_NOT_READY).
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
public class NoOpLatencyBenchmark {

    private static final int STACK_DEPTH = 150;

    private static final FlagEvaluationOptions NON_ERROR_HOOK = FlagEvaluationOptions.builder()
            .hook(new BooleanHook() {
                @Override
                public void finallyAfter(
                        HookContext<Boolean> ctx, FlagEvaluationDetails<Boolean> details, Map<String, Object> hints) {}
            })
            .build();
    private static final FlagEvaluationOptions ERROR_HOOK = FlagEvaluationOptions.builder()
            .hook(new BooleanHook() {
                @Override
                public void error(HookContext<Boolean> ctx, Exception error, Map<String, Object> hints) {}
            })
            .build();

    private Client client;

    @Setup
    public void setup() {
        OpenFeatureAPI api = OpenFeatureAPI.getInstance();
        api.setEvaluationContext(new MutableContext());
        api.setTransactionContextPropagator(new ThreadLocalTransactionContextPropagator());
        client = api.getClient();
    }

    @Benchmark
    @Threads(1)
    public boolean noContext() {
        return client.getBooleanValue("my-flag", false);
    }

    @Benchmark
    @Threads(1)
    public boolean invocationUserContext() {
        return client.getBooleanValue(
                "my-flag",
                false,
                new ImmutableContext(Map.of("email", new Value("user@example.com"), "userId", new Value("user-123"))));
    }

    @Benchmark
    @Threads(1)
    public boolean transactionUserContext() {
        OpenFeatureAPI.getInstance()
                .setTransactionContext(new ImmutableContext(
                        Map.of("email", new Value("user@example.com"), "userId", new Value("user-123"))));
        return client.getBooleanValue("my-flag", false);
    }

    @Benchmark
    @Threads(1)
    public boolean nonErrorHook() {
        return client.getBooleanValue("my-flag", false, new ImmutableContext(), NON_ERROR_HOOK);
    }

    @Benchmark
    @Threads(1)
    public boolean errorHook() {
        return client.getBooleanValue("my-flag", false, new ImmutableContext(), ERROR_HOOK);
    }

    @Benchmark
    @Threads(1)
    public boolean noContextDeepStack() {
        return atDepth(STACK_DEPTH);
    }

    private boolean atDepth(int depth) {
        // simulate a realistic server call stack
        return depth == 0 ? client.getBooleanValue("my-flag", false) : atDepth(depth - 1);
    }

    @Benchmark
    @Threads(8)
    public boolean invocationUserContext8Threads() {
        return client.getBooleanValue(
                "my-flag",
                false,
                new ImmutableContext(Map.of("email", new Value("user@example.com"), "userId", new Value("user-123"))));
    }
}
