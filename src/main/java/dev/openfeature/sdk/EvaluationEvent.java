package dev.openfeature.sdk;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * Represents an evaluation event.
 */
@Builder
@Getter
public class EvaluationEvent {

    private String name;

    @Singular("attribute")
    private Map<String, Object> attributes;

    @Singular("bodyElement")
    private Map<String, Object> body;

    public Map<String, Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    public Map<String, Object> getBody() {
        return new HashMap<>(body);
    }
}
