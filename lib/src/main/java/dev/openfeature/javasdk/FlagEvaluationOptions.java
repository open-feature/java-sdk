package dev.openfeature.javasdk;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data @Builder
public class FlagEvaluationOptions {
    // I guess because we are using boxed types, we can pass "Object" as T everywhere to avoid the
    // "raw types" warning, but I'm not sure if that's really too helpful beyond removing some warnings
    @Singular private List<Hook<Object>> hooks;
    @Builder.Default
    private ImmutableMap<String, Object> hookHints = ImmutableMap.of();
}
