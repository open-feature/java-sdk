package javasdk;

import java.lang.annotation.Repeatable;

@Repeatable(Specifications.class)
public @interface Specification {
    String spec();
    String number();
    String text();
}
