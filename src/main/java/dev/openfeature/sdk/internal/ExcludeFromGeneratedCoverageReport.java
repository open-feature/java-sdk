package dev.openfeature.sdk.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JaCoCo ignores coverage of methods annotated with any annotation with "generated" in the name.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExcludeFromGeneratedCoverageReport {
}
