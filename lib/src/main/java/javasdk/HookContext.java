package javasdk;

import lombok.Data;

@Data
public class HookContext<T> {
    String flagKey;
    FlagValueType type;
    Client client;
    EvaluationContext ctx;
    FeatureProvider provider;
    T defaultValue;
    HookEvaluation<T> executedHooks;

    public static <T> HookContext<T> from(String key, FlagValueType type, Client client, EvaluationContext ctx, T defaultValue) {
        HookContext<T> hc = new HookContext<>();
        hc.flagKey = key;
        hc.type = type;
        hc.client = client;
        hc.ctx = ctx;
        hc.executedHooks = new HookEvaluation<T>();
        hc.defaultValue = defaultValue;
        return hc;
    }
}
