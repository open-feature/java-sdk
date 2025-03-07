package dev.openfeature.sdk.e2e;

import dev.openfeature.sdk.Client;
import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.FeatureProvider;
import dev.openfeature.sdk.FlagEvaluationDetails;
import dev.openfeature.sdk.MutableContext;
import java.util.List;

public class State {
    public Client client;
    public Flag flag;
    public MutableContext context = new MutableContext();
    public FlagEvaluationDetails evaluation;
    public MockHook hook;
    public FeatureProvider provider;
    public EvaluationContext invocationContext;
    public List<String> levels;
}
