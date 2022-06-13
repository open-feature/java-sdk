package dev.openfeature.javasdk;

public interface Client extends FlagEvaluationLifecycle, Features {
    Metadata getMetadata();
}
