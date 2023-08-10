package dev.openfeature.sdk.providers.memory;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.Map;

/**
 * Flags representation.
 */
@ToString
@Getter
@Builder
public class Flags {

    @Singular
    private Map<String, Flag<?>> flags;
}
