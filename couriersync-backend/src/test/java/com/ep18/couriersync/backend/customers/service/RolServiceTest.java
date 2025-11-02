package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.common.dto.PagingDTOs.PageResponse;
import com.ep18.couriersync.backend.common.exception.ConflictException;
import com.ep18.couriersync.backend.common.exception.NotFoundException;
import com.ep18.couriersync.backend.customers.domain.Rol;
import com.ep18.couriersync.backend.customers.dto.RolDTOs.CreateRolInput;
import com.ep18.couriersync.backend.customers.dto.RolDTOs.UpdateRolInput;
import com.ep18.couriersync.backend.customers.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RolServiceTest {

    @Mock
    private RolRepository rolRepo;

    @InjectMocks
    private RolService rolService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateRol_Success() {
        CreateRolInput input = new CreateRolInput("Admin");
        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombreRol("Admin");

        when(rolRepo.existsByNombreRolIgnoreCase("Admin")).thenReturn(false);
        when(rolRepo.save(any(Rol.class))).thenReturn(rol);

        var result = rolService.create(input);

        assertNotNull(result);
        assertEquals("Admin", result.nombreRol());
        assertEquals(1, result.idRol());
    }

    @Test
    void testCreateRol_Conflict() {
        CreateRolInput input = new CreateRolInput("Admin");
        when(rolRepo.existsByNombreRolIgnoreCase("Admin")).thenReturn(true);

        assertThrows(ConflictException.class, () -> rolService.create(input));
    }

    @Test
    void testUpdateRol_Success() {
        UpdateRolInput input = new UpdateRolInput(1, "User");
        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombreRol("Admin");

        when(rolRepo.findById(1)).thenReturn(Optional.of(rol));
        when(rolRepo.existsByNombreRolIgnoreCase("User")).thenReturn(false);
        when(rolRepo.save(any(Rol.class))).thenAnswer(i -> i.getArgument(0));

        var result = rolService.update(input);

        assertEquals("User", result.nombreRol());
    }

    @Test
    void testUpdateRol_NotFound() {
        UpdateRolInput input = new UpdateRolInput(1, "User");
        when(rolRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> rolService.update(input));
    }

    @Test
    void testFindById_Success() {
        Rol rol = new Rol();
        rol.setIdRol(1);
        rol.setNombreRol("Admin");

        when(rolRepo.findById(1)).thenReturn(Optional.of(rol));

        var result = rolService.findById(1);

        assertEquals("Admin", result.nombreRol());
    }

    @Test
    void testFindById_NotFound() {
        when(rolRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> rolService.findById(1));
    }

    @Test
    void testList_Success() {
        Rol rol1 = new Rol();
        rol1.setIdRol(1);
        rol1.setNombreRol("Admin");

        Rol rol2 = new Rol();
        rol2.setIdRol(2);
        rol2.setNombreRol("User");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("nombreRol").ascending());
        Page<Rol> page = new PageImpl<>(List.of(rol1, rol2), pageable, 2);

        // Mock correcto para findAll(Pageable)
        when(rolRepo.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<?> result = rolService.list(0, 10);

        assertEquals(2, result.content().size());
    }

    @Test
    void testDelete_Success() {
        when(rolRepo.existsById(1)).thenReturn(true);
        doNothing().when(rolRepo).deleteById(1);

        boolean result = rolService.delete(1);
        assertTrue(result);
    }

    @Test
    void testDelete_NotFound() {
        when(rolRepo.existsById(1)).thenReturn(false);
        boolean result = rolService.delete(1);
        assertFalse(result);
    }
}
