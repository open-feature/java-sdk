package dev.openfeature.sdk;

public class HookContextWithData<T> extends HookContext<T> {

    private HookContextWithData(String flagKey, FlagValueType type, T defaultValue,
            EvaluationContext ctx, ClientMetadata clientMetadata, Metadata providerMetadata, HookData data) {
        super(flagKey, type, defaultValue, ctx, clientMetadata, providerMetadata, data);
    }

    public static <T> HookContextWithData<T> of(HookContext<T> context, HookData data) {
        return new HookContextWithData<>(context.getFlagKey(), context.getType(), context.getDefaultValue(), context.getCtx(), context.getClientMetadata(),
                context.getProviderMetadata(), data);
    }
}
