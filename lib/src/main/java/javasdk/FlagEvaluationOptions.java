package javasdk;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data @Builder
public class FlagEvaluationOptions {
    @Singular private List<Hook> hooks;
}
