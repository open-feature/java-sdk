package dev.openfeature.api.evaluation;

import dev.openfeature.api.Hook;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("checkstyle:MissingJavadocType")
public final class FlagEvaluationOptions {
    private final List<Hook<?>> hooks;
    private final Map<String, Object> hookHints;

    public FlagEvaluationOptions() {
        this.hooks = new ArrayList<>();
        this.hookHints = new HashMap<>();
    }

    public FlagEvaluationOptions(List<Hook<?>> hooks, Map<String, Object> hookHints) {
        this.hooks = hooks != null ? new ArrayList<>(hooks) : new ArrayList<>();
        this.hookHints = hookHints != null ? new HashMap<>(hookHints) : new HashMap<>();
    }

    public List<Hook<?>> getHooks() {
        return new ArrayList<>(hooks);
    }

    public Map<String, Object> getHookHints() {
        return new HashMap<>(hookHints);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FlagEvaluationOptions that = (FlagEvaluationOptions) obj;
        return Objects.equals(hooks, that.hooks) && Objects.equals(hookHints, that.hookHints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hooks, hookHints);
    }

    @Override
    public String toString() {
        return "FlagEvaluationOptions{" + "hooks=" + hooks + ", hookHints=" + hookHints + '}';
    }

    public static class Builder {
        private List<Hook<?>> hooks = new ArrayList<>();
        private Map<String, Object> hookHints = new HashMap<>();

        public Builder hooks(List<Hook<?>> hooks) {
            this.hooks = hooks != null ? new ArrayList<>(hooks) : new ArrayList<>();
            return this;
        }

        public Builder hook(Hook<?> hook) {
            this.hooks.add(hook);
            return this;
        }

        public Builder hookHints(Map<String, Object> hookHints) {
            this.hookHints = hookHints != null ? new HashMap<>(hookHints) : new HashMap<>();
            return this;
        }

        public FlagEvaluationOptions build() {
            return new FlagEvaluationOptions(hooks, hookHints);
        }
    }
}
