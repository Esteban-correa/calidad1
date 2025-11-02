package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.CreateUsuarioInput;
import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.UpdateUsuarioInput;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioValidatorTest {

    private final Validator validator;

    UsuarioValidatorTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void testCreateUsuarioInput_Valid() {
        CreateUsuarioInput input = new CreateUsuarioInput(
                "Juan Perez",
                "juan@example.com",
                "1234567890",
                LocalDate.now(),
                "Direccion prueba",
                1,
                1,
                1
        );

        Set<ConstraintViolation<CreateUsuarioInput>> violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "No debería haber violaciones de validación");
    }

    @Test
    void testCreateUsuarioInput_InvalidEmail() {
        CreateUsuarioInput input = new CreateUsuarioInput(
                "Juan Perez",
                "correo_invalido",
                "1234567890",
                LocalDate.now(),
                "Direccion prueba",
                1,
                1,
                1
        );

        Set<ConstraintViolation<CreateUsuarioInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty(), "Debe detectar email inválido");
    }

    @Test
    void testUpdateUsuarioInput_NullFields() {
        UpdateUsuarioInput input = new UpdateUsuarioInput(
                1,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<UpdateUsuarioInput>> violations = validator.validate(input);
        assertTrue(violations.isEmpty(), "Campos opcionales pueden ser nulos");
    }

    @Test
    void testUpdateUsuarioInput_InvalidPhone() {
        UpdateUsuarioInput input = new UpdateUsuarioInput(
                1,
                "Juan",
                "juan@example.com",
                "123", // teléfono inválido
                LocalDate.now(),
                "Direccion",
                1,
                1,
                1
        );

        Set<ConstraintViolation<UpdateUsuarioInput>> violations = validator.validate(input);
        assertFalse(violations.isEmpty(), "Debe detectar teléfono inválido");
    }
}
