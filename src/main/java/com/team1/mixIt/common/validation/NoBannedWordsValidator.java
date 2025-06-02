package com.team1.mixIt.common.validation;

import com.team1.mixIt.common.config.BannedWordsConfig;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NoBannedWordsValidator implements ConstraintValidator<NoBannedWords, String> {

    private final BannedWordsConfig config;
    private static final Pattern WHOLE_WORD_GOCHU = Pattern.compile("(^|\\s)고추(\\s|$)");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;

        List<String> banned = config.getBannedWords();
        for (String word : banned) {
            if ("고추".equals(word)) {
                // “고추”만 단독으로 들어간 경우에만 금칙
                if (WHOLE_WORD_GOCHU.matcher(value).find()) {
                    context.disableDefaultConstraintViolation();
                    context
                            .buildConstraintViolationWithTemplate("금칙어 \"고추\"가 포함되어 있습니다.")
                            .addConstraintViolation();
                    return false;
                }
            } else {
                // 그 외 금칙어는 기존처럼 단순 포함 검사
                if (value.contains(word)) {
                    context.disableDefaultConstraintViolation();
                    context
                            .buildConstraintViolationWithTemplate(
                                    String.format("금칙어 \"%s\"가 포함되어 있습니다.", word)
                            )
                            .addConstraintViolation();
                    return false;
                }
            }
        }
        return true;
    }
}
