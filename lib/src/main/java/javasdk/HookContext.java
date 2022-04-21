package javasdk;

import java.util.List;

public class HookContext<T> {
    String flagKey;
    FlagValueType type;
    // TODO: Fix this when we actually have a client class.
//    Client client;
    EvaluationContext ctx;
    FeatureProvider provider;
    T defaultValue;
    List<Hook> executedHooks;
}
