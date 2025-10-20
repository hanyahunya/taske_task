package com.hanyahunya.task.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsAdminRoleValidator implements ConstraintValidator<IsAdminRole, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }

        return" ROLE_ADMIN".equals(value);
    }
}