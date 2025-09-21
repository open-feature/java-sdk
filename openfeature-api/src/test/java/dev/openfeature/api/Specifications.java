package dev.openfeature.api;

/**
 * Reference a list of specification a test matches.
 */
public @interface Specifications {
    Specification[] value();
}
