package dev.openfeature.sdk.benchmark;

import dev.openfeature.sdk.benchmark.state.FlagEvaluationState;
import dev.openfeature.sdk.benchmark.state.HooksState;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.Throughput)
@Warmup(time = 1, timeUnit = TimeUnit.SECONDS, iterations = 1)
// @Warmup(time = 1, timeUnit = TimeUnit.SECONDS, iterations = 2)
@Measurement(time = 5, timeUnit = TimeUnit.SECONDS, iterations = 1)
// @Measurement(time = 5, timeUnit = TimeUnit.SECONDS, iterations = 4)
@Fork(1)
public class FlagEvaluationBenchmark {

    @Benchmark
    public String flagEvaluations(FlagEvaluationState state) {
        return state.client
                .getStringDetails(FlagEvaluationState.FLAG_KEY, "default")
                .getValue();
    }

    @Benchmark
    public String hookExecution(HooksState state) {
        return state.client.getStringDetails(HooksState.FLAG_KEY, "default").getValue();
    }
}
