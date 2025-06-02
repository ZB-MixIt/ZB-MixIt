package com.team1.mixIt.common.validation;

import com.team1.mixIt.common.config.BannedWordsConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NoBannedWordsValidator implements ConstraintValidator<NoBannedWords, String> {

    private final BannedWordsConfig config;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        for (String banned : config.getBannedWords()) {
            if (value.contains(banned)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        String.format("금칙어 \"%s\"가 포함되어 있습니다.", banned)
                ).addConstraintViolation();
                return false;
            }
        }

        return true;
    }
}
