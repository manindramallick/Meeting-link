package com.demo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method for header validation by {@code HeaderValidationAspect}.
 *
 * <p>The aspect will:
 * <ul>
 *   <li>Assert that the {@code env} header is present and non-blank.</li>
 *   <li>Assert that the {@code env} value belongs to the allowed set.</li>
 *   <li>Optionally assert that a {@code X-Correlation-Id} header is present
 *       when {@code requireCorrelationId = true} (default {@code false}).</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateEnvHeader {

    /**
     * When {@code true} the aspect also checks that the
     * {@code X-Correlation-Id} header is present and non-blank.
     */
    boolean requireCorrelationId() default false;
}

