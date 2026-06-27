package com.codewithankita.aibugtracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SafeTextValidator implements ConstraintValidator<SafeText, String> {

    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
            "<[^>]*>|javascript:|on\\w+=|'\\s*OR\\s*'|'\\s*--|DROP\\s+|DELETE\\s+|INSERT\\s+|EXEC\\s+|UNION\\s+",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !UNSAFE_PATTERN.matcher(value).find();
    }
}
