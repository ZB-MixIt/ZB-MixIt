package com.team1.mixIt.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxFileSizeValidator.class)
@Documented
public @interface MaxFileSize {
    String message() default "파일 크기가 너무 큽니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default{};
    long value();
}
