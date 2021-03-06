package com.cursomc.services.validation;

import javax.validation.Payload;

public @interface ClienteUpdate {
    String message() default "Erro de validação";
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
