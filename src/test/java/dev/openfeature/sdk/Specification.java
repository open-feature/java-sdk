package dev.openfeature.sdk;

import java.lang.annotation.Repeatable;

@Repeatable(Specifications.class)
public @interface Specification {
    String number();
    String text();
}
