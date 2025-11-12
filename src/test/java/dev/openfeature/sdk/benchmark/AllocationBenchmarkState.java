package dev.openfeature.sdk.benchmark;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.ImmutableContext;
import dev.openfeature.sdk.NoOpProvider;
import dev.openfeature.sdk.OpenFeatureAPI;
import dev.openfeature.sdk.ThreadLocalTransactionContextPropagator;
import dev.openfeature.sdk.Value;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import java.util.HashMap;
import java.util.Map;

@State(Scope.Benchmark)
public class AllocationBenchmarkState {
    public final Client client;
    public final Map<String, Value> transactionAttr;
    public final Map<String, Value> transactionAttr2;
    public final EvaluationContext invocationContext;

    public AllocationBenchmarkState(){
        long start = System.currentTimeMillis();
        OpenFeatureAPI.getInstance().setProviderAndWait(new NoOpProvider());
        OpenFeatureAPI.getInstance().setTransactionContextPropagator(new ThreadLocalTransactionContextPropagator());
        long end = System.currentTimeMillis();
        System.out.println("Setup time: " + (end - start) + "ms");
        Map<String, Value> globalAttrs = new HashMap<>();
        globalAttrs.put("global", new Value(1));
        EvaluationContext globalContext = new ImmutableContext(globalAttrs);
        OpenFeatureAPI.getInstance().setEvaluationContext(globalContext);

        client = OpenFeatureAPI.getInstance().getClient();

        Map<String, Value> clientAttrs = new HashMap<>();
        clientAttrs.put("client", new Value(2));
        client.setEvaluationContext(new ImmutableContext(clientAttrs));

        transactionAttr = new HashMap<>();
        transactionAttr.put("trans", new Value(4));

        transactionAttr2 = new HashMap<>();
        transactionAttr2.put("trans2", new Value(5));

        Map<String, Value> invocationAttrs = new HashMap<>();
        invocationAttrs.put("invoke", new Value(3));
        invocationContext = new ImmutableContext(invocationAttrs);

    }
}
