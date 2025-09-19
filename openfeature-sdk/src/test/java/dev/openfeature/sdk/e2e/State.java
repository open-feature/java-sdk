package dev.openfeature.sdk.e2e;

import dev.openfeature.api.Client;
import dev.openfeature.api.OpenFeatureAPI;
import dev.openfeature.api.Provider;
import dev.openfeature.api.evaluation.EvaluationContext;
import dev.openfeature.api.evaluation.FlagEvaluationDetails;
import dev.openfeature.api.evaluation.MutableContext;
import dev.openfeature.sdk.DefaultOpenFeatureAPIProvider;
import java.util.List;

public class State {
    public OpenFeatureAPI api = new DefaultOpenFeatureAPIProvider().createAPI();
    public Client client;
    public Flag flag;
    public MutableContext context = new MutableContext();
    public FlagEvaluationDetails evaluation;
    public MockHook hook;
    public Provider provider;
    public EvaluationContext invocationContext;
    public List<String> levels;
}
