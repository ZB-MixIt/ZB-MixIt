package com.team1.mixIt.common.validation;

import com.team1.mixIt.user.entity.User;
import com.team1.mixIt.user.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NoPersonalInfoValidator implements ConstraintValidator<NoPersonalInfo, String> {

    // 주민등록번호
    private static final Pattern SSN = Pattern.compile("\\b\\d{6}-\\d{7}\\b");
    // 휴대폰번호
    private static final Pattern PHONE = Pattern.compile("\\b0\\d{1,2}-\\d{3,4}-\\d{4}\\b");
    // 이메일
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    // 대출 접미사 키워드
    private static final Pattern LOAN_SUFFIX = Pattern.compile("(?i)\\b\\w*대출\\b");

    private final UserRepository userRepository;
    private List<String> forbiddenNames;

    @Override
    public void initialize(NoPersonalInfo ann) {
        forbiddenNames = userRepository.findAll().stream()
                .map(User::getName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        // 주민등록번호
        if (SSN.matcher(value).find()) {
            return false;
        }
        // 휴대폰번호, 이메일
        if (PHONE.matcher(value).find() || EMAIL.matcher(value).find()) {
            return false;
        }
        // 사용자 실명
        for (String name : forbiddenNames) {
            if (value.contains(name)) {
                return false;
            }
        }
        // 대출 접미사 키워드
        if (LOAN_SUFFIX.matcher(value).find()) {
            return false;
        }

        return true;
    }
}
