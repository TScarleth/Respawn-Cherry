package com.respawncherry.usuarios.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ContrasenaSeguraValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContrasenaSegura {

    String message() default "La contraseña debe tener entre 8 y 72 caracteres, una letra, un número y un carácter especial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
