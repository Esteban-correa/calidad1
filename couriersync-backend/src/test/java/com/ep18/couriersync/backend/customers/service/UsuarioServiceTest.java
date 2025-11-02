package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.common.exception.NotFoundException;
import com.ep18.couriersync.backend.customers.domain.*;
import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.UsuarioView;
import com.ep18.couriersync.backend.customers.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void testFindById_WhenUsuarioExists() {
        // --- Arrange ---
        Ciudad ciudad = new Ciudad();
        ciudad.setIdCiudad(10);
        ciudad.setNombreCiudad("Bogotá");

        Departamento depto = new Departamento();
        depto.setIdDepartamento(5);
        depto.setNombreDepartamento("Cundinamarca");

        Rol rol = new Rol();
        rol.setIdRol(3);
        rol.setNombreRol("Administrador");

        Usuario mockUsuario = new Usuario();
        mockUsuario.setIdUsuario(1);
        mockUsuario.setNombre("Esteban");
        mockUsuario.setCorreo("correo@ejemplo.com");
        mockUsuario.setTelefono("1234567890");
        mockUsuario.setFechaRegistro(LocalDate.now());
        mockUsuario.setDetalleDireccion("Calle Falsa 123");
        mockUsuario.setCiudad(ciudad);
        mockUsuario.setDepartamento(depto);
        mockUsuario.setRol(rol);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(mockUsuario));

        // --- Act ---
        UsuarioView result = usuarioService.findById(1);

        // --- Assert ---
        assertNotNull(result);
        assertEquals(1, result.idUsuario());
        assertEquals("Esteban", result.nombre());
        assertEquals("Bogotá", result.nombreCiudad());
        assertEquals("Cundinamarca", result.nombreDepartamento());
        assertEquals("Administrador", result.nombreRol());

        verify(usuarioRepository, times(1)).findById(1);
    }

    @Test
    void testFindById_WhenUsuarioNotFound() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> usuarioService.findById(99));

        verify(usuarioRepository, times(1)).findById(99);
    }
}
