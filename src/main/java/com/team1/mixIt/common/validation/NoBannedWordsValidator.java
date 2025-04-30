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
        if (value == null) return true;
        return !config.containsBanned(value);
    }
}
