package com.team1.mixIt.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Set;

public class RequireOneParamValidator implements ConstraintValidator<RequireOneParam, Object> {
    private static final Set<Class<?>> BOXED_TYPES = Set.of(
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class
    );

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) {
            return true;
        }

        Field[] fields = value.getClass().getDeclaredFields();
        context.disableDefaultConstraintViolation();

        int providedCnt = 0;
        for (Field field : fields) {
            if (!isBoxed(field)) throw new RuntimeException("Target field must be boxed or String type");
            try {
                field.setAccessible(true);
                if (!Objects.isNull(field.get(value))) providedCnt++;
                context.buildConstraintViolationWithTemplate(
                                "Only one parameter should be entered."
                        )
                        .addPropertyNode(field.getName())
                        .addConstraintViolation();
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access fieldName" + field.getName(), e);
            }
        }
        return providedCnt == 1;
    }

    private boolean isBoxed(Field field) {
        if (field == null) {
            return false;
        }
        return BOXED_TYPES.contains(field.getType()) || field.getType() == String.class;
    }
}
