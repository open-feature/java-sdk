package dev.openfeature.api;

import java.lang.annotation.Repeatable;

/**
 * Reference the specification a test matches.
 */
@Repeatable(Specifications.class)
public @interface Specification {
    String number();

    String text();
}
