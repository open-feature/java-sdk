package dev.openfeature.javasdk;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import javax.annotation.Nullable;

@Value @Builder @With
public class HookContext<T> {
    @NonNull String flagKey;
    @NonNull FlagValueType type;
    @NonNull T defaultValue;
    @Nullable EvaluationContext ctx;
    Client client;
    FeatureProvider provider;

    public static <T> HookContext<T> from(String key, FlagValueType type, Client client, EvaluationContext ctx, T defaultValue) {
        return HookContext.<T>builder()
                .flagKey(key)
                .type(type)
                .client(client)
                .ctx(ctx)
                .defaultValue(defaultValue)
                .build();
    }
}
