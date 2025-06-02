package com.team1.mixIt.common.validation;

import com.team1.mixIt.common.config.BannedWordsConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
public class NoBannedWordsValidator implements ConstraintValidator<NoBannedWords, String> {
    private final BannedWordsConfig config;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        Pattern forbidden = Pattern.compile("\\b고추\\b");
        if (forbidden.matcher(value).find()) {
            return false;
        }
        return !config.containsBanned(value);
    }
}
