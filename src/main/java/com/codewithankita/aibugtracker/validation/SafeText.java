package com.codewithankita.aibugtracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeTextValidator.class)
public @interface SafeText {
    String message() default "Input contains prohibited characters or patterns";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
