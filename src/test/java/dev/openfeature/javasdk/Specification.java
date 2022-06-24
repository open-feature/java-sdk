package dev.openfeature.javasdk;

import java.lang.annotation.Repeatable;

@Repeatable(Specifications.class)
public @interface Specification {
    String number();
    String text();
}
