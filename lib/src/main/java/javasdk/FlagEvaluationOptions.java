package javasdk;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;
import java.util.Map;

@Data @Builder
public class FlagEvaluationOptions {
    @Singular private List<Hook> hooks;
    private Map<String, Object> hookHints;
}
