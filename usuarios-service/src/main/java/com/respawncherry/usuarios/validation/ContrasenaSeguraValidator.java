package com.respawncherry.usuarios.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContrasenaSeguraValidator implements ConstraintValidator<ContrasenaSegura, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        boolean largoMinimo = value.length() >= 8;
        boolean largoMaximo = value.length() <= 72;
        boolean letra = value.chars().anyMatch(Character::isLetter);
        boolean numero = value.chars().anyMatch(Character::isDigit);
        boolean especial = value.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        return largoMinimo && largoMaximo && letra && numero && especial;
    }
}
