package com.team1.mixIt.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileTypeValidator.class)
@Documented
public @interface FileType {
    String message() default "지원하지 않는 파일 형식입니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default{};
    String[] extensions();
}
