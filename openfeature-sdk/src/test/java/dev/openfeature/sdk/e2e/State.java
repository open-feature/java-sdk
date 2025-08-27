package dev.openfeature.sdk.e2e;

import dev.openfeature.api.Client;
import dev.openfeature.api.EvaluationContext;
import dev.openfeature.api.FeatureProvider;
import dev.openfeature.api.FlagEvaluationDetails;
import dev.openfeature.api.MutableContext;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.sdk.DefaultOpenFeatureAPIProvider;
import java.util.List;

public class State {
    public OpenFeatureAPI api = new DefaultOpenFeatureAPIProvider().createAPI();
    public Client client;
    public Flag flag;
    public MutableContext context = new MutableContext();
    public FlagEvaluationDetails evaluation;
    public MockHook hook;
    public FeatureProvider provider;
    public EvaluationContext invocationContext;
    public List<String> levels;
}
