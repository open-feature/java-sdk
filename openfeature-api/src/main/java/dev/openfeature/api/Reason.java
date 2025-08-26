package dev.openfeature.api;

/**
 * Predefined resolution reasons.
 */
public enum Reason {
    DISABLED,
    SPLIT,
    TARGETING_MATCH,
    DEFAULT,
    UNKNOWN,
    CACHED,
    STATIC,
    ERROR
}
