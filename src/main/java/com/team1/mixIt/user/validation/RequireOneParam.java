package com.team1.mixIt.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = RequireOneParamValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOneParam {
    String message() default "Invalid query param.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
