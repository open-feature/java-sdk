package dev.openfeature.api;

import java.util.Map;

/**
 * Represents an evaluation event.
 * This class is immutable and thread-safe.
 */
public interface EvaluationEvent {
    String getName();

    Map<String, Object> getAttributes();
}
