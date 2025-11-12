package dev.openfeature.sdk.benchmark;

import static dev.openfeature.sdk.testutils.TestFlagsUtils.BOOLEAN_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.FLOAT_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.INT_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.OBJECT_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.STRING_FLAG_KEY;

import dev.openfeature.sdk.BooleanHook;
import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.DoubleHook;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.ImmutableStructure;
import dev.openfeature.sdk.IntegerHook;
import dev.openfeature.sdk.NoOpProvider;
import dev.openfeature.sdk.ObjectHook;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.StringHook;
import dev.openfeature.sdk.Value;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Runs a large volume of flag evaluations on a VM with 1G memory and GC
 * completely disabled, so we can take a heap-dump.
 */
public class AllocationBenchmark {

    // 10K iterations works well with Xmx1024m (we don't want to run out of memory)
    private static final int ITERATIONS = 10000;

    // @Benchmark
    // @BenchmarkMode(Mode.SingleShotTime)
    // @Fork(jvmArgsAppend = {"-Xmx1024m", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"})
    public void run() {

        OpenFeatureAPI.getInstance().setProviderAndWait(new NoOpProvider());
        Map<String, Value> globalAttrs = new HashMap<>();
        globalAttrs.put("global", new Value(1));
        EvaluationContext globalContext = new ImmutableContext(globalAttrs);
        OpenFeatureAPI.getInstance().setEvaluationContext(globalContext);

        Client client = OpenFeatureAPI.getInstance().getClient();

        Map<String, Value> clientAttrs = new HashMap<>();
        clientAttrs.put("client", new Value(2));
        client.setEvaluationContext(new ImmutableContext(clientAttrs));
        client.addHooks(new ObjectHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Object> ctx, Map<String, Object> hints) {
                return Optional.ofNullable(new ImmutableContext());
            }
        });
        client.addHooks(new StringHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<String> ctx, Map<String, Object> hints) {
                return Optional.ofNullable(new ImmutableContext());
            }
        });
        client.addHooks(new BooleanHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Boolean> ctx, Map<String, Object> hints) {
                return Optional.ofNullable(new ImmutableContext());
            }
        });
        client.addHooks(new IntegerHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Integer> ctx, Map<String, Object> hints) {
                return Optional.ofNullable(new ImmutableContext());
            }
        });
        client.addHooks(new DoubleHook() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Double> ctx, Map<String, Object> hints) {
                return Optional.ofNullable(new ImmutableContext());
            }
        });

        Map<String, Value> invocationAttrs = new HashMap<>();
        invocationAttrs.put("invoke", new Value(3));
        EvaluationContext invocationContext = new ImmutableContext(invocationAttrs);

        for (int i = 0; i < ITERATIONS; i++) {
            client.getBooleanValue(BOOLEAN_FLAG_KEY, false);
            client.getStringValue(STRING_FLAG_KEY, "default");
            client.getIntegerValue(INT_FLAG_KEY, 0);
            client.getDoubleValue(FLOAT_FLAG_KEY, 0.0);
            client.getObjectDetails(OBJECT_FLAG_KEY, new Value(new ImmutableStructure()), invocationContext);
        }
    }

    @Benchmark
    //@BenchmarkMode(Mode.SingleShotTime)
    @Fork(jvmArgsAppend = {"-Xmx1024m", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"})
    //@Test
    public void context(Blackhole blackhole, AllocationBenchmarkState state) {
        OpenFeatureAPI.getInstance().setTransactionContext(new ImmutableContext(state.transactionAttr));

        for (int j = 0; j < 2; j++) {
            blackhole.consume(state.client.getBooleanValue(BOOLEAN_FLAG_KEY, false));
            blackhole.consume(state.client.getStringValue(STRING_FLAG_KEY, "default"));
            blackhole.consume(state.client.getIntegerValue(INT_FLAG_KEY, 0, state.invocationContext));
            blackhole.consume(state.client.getDoubleValue(FLOAT_FLAG_KEY, 0.0));
            blackhole.consume(state.client.getObjectDetails(OBJECT_FLAG_KEY, new Value(new ImmutableStructure()),
                    state.invocationContext));
        }

        OpenFeatureAPI.getInstance().setTransactionContext(new ImmutableContext(state.transactionAttr2));

        for (int j = 0; j < 2; j++) {
            blackhole.consume(state.client.getBooleanValue(BOOLEAN_FLAG_KEY, false));
            blackhole.consume(state.client.getStringValue(STRING_FLAG_KEY, "default"));
            blackhole.consume(state.client.getIntegerValue(INT_FLAG_KEY, 0, state.invocationContext));
            blackhole.consume(state.client.getDoubleValue(FLOAT_FLAG_KEY, 0.0));
            blackhole.consume(state.client.getObjectDetails(OBJECT_FLAG_KEY, new Value(new ImmutableStructure()),
                    state.invocationContext));
        }
    }
}
