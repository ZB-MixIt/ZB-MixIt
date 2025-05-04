package com.team1.mixIt.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class MaxFileSizeValidator implements ConstraintValidator<MaxFileSize, MultipartFile> {
    private long max;
    @Override public void initialize(MaxFileSize ann) { this.max = ann.value(); }
    @Override public boolean isValid(MultipartFile file, ConstraintValidatorContext ctx) {
        return file == null || file.isEmpty() || file.getSize() <= max;
    }
}
