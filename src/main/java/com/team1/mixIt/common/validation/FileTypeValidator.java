// FileTypeValidator.java
package com.team1.mixIt.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {
    private List<String> exts;
    @Override public void initialize(FileType ann) {
        this.exts = Arrays.stream(ann.extensions())
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }
    @Override public boolean isValid(MultipartFile file, ConstraintValidatorContext ctx) {
        if (file == null || file.isEmpty()) return true;
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) return false;
        String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        return exts.contains(ext);
    }
}
