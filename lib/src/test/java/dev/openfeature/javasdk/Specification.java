package dev.openfeature.javasdk;

import java.lang.annotation.Repeatable;

// This is a cool annotation. I may look into doing something similar by wrapping jest.describe.
@Repeatable(Specifications.class)
public @interface Specification {
    String spec();
    String number();
    String text();
}
