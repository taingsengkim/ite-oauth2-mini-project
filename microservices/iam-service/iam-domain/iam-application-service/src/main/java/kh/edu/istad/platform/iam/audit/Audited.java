package kh.edu.istad.platform.iam.audit;

import java.lang.annotation.*;

/** Expressions are developer-owned and select identifiers only; request/response bodies are never serialized. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {
    AuditAction action();
    String target();
    String related() default "";
    String expiry() default "";
    String effect() default "";
    String[] fields() default {};
    String fieldSource() default "";
    int fieldArgument() default 0;
}
