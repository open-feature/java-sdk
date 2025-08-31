package dev.openfeature.sdk.benchmark;

import static dev.openfeature.sdk.testutils.TestFlagsUtils.BOOLEAN_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.FLOAT_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.INT_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.OBJECT_FLAG_KEY;
import static dev.openfeature.sdk.testutils.TestFlagsUtils.STRING_FLAG_KEY;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.Hook;
import dev.openfeature.sdk.HookContext;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.ImmutableStructure;
import dev.openfeature.sdk.NoOpProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.Value;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;

/**
 * Runs a large volume of flag evaluations on a VM with 1G memory and GC
 * completely disabled so we can take a heap-dump.
 */
public class AllocationBenchmark {

    // 10K iterations works well with Xmx1024m (we don't want to run out of memory)
    private static final int ITERATIONS = 10000;

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Fork(jvmArgsAppend = {"-Xmx1024m", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"})
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
        client.addHooks(new Hook<Object>() {
            @Override
            public Optional<EvaluationContext> before(HookContext<Object> ctx, Map<String, Object> hints) {
                return Optional.ofNullable(new ImmutableContext());
            }
        });

        Map<String, Value> invocationAttrs = new HashMap<>();
        invocationAttrs.put("invoke", new Value(3));
        EvaluationContext invocationContext = new ImmutableContext(invocationAttrs);

        for (int i = 0; i < ITERATIONS; i++) {
            System.out.println("Iteration");
            client.getBooleanValue(BOOLEAN_FLAG_KEY, false);
            client.getStringValue(STRING_FLAG_KEY, "default");
            client.getIntegerValue(INT_FLAG_KEY, 0);
            client.getDoubleValue(FLOAT_FLAG_KEY, 0.0);
            client.getObjectDetails(OBJECT_FLAG_KEY, new Value(new ImmutableStructure()), invocationContext);
        }
    }
}
