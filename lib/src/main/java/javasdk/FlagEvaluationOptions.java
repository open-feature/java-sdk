package javasdk;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data @Builder
public class FlagEvaluationOptions {
    @Singular private List<Hook> hooks;
    @Builder.Default
    private ImmutableMap<String, Object> hookHints = ImmutableMap.of();
}
