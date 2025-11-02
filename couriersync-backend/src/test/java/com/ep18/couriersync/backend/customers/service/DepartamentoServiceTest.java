package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.common.dto.PagingDTOs.PageResponse;
import com.ep18.couriersync.backend.common.exception.ConflictException;
import com.ep18.couriersync.backend.common.exception.NotFoundException;
import com.ep18.couriersync.backend.customers.domain.Departamento;
import com.ep18.couriersync.backend.customers.dto.DepartamentoDTOs.CreateDepartamentoInput;
import com.ep18.couriersync.backend.customers.dto.DepartamentoDTOs.UpdateDepartamentoInput;
import com.ep18.couriersync.backend.customers.repository.DepartamentoRepository;
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

class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepo;

    @InjectMocks
    private DepartamentoService departamentoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateDepartamento_Success() {
        CreateDepartamentoInput input = new CreateDepartamentoInput("Lima");
        Departamento dep = new Departamento();
        dep.setIdDepartamento(1);
        dep.setNombreDepartamento("Lima");

        when(departamentoRepo.existsByNombreDepartamentoIgnoreCase("Lima")).thenReturn(false);
        when(departamentoRepo.save(any(Departamento.class))).thenReturn(dep);

        var result = departamentoService.create(input);

        assertNotNull(result);
        assertEquals("Lima", result.nombreDepartamento());
        assertEquals(1, result.idDepartamento());
    }

    @Test
    void testCreateDepartamento_Conflict() {
        CreateDepartamentoInput input = new CreateDepartamentoInput("Lima");
        when(departamentoRepo.existsByNombreDepartamentoIgnoreCase("Lima")).thenReturn(true);

        assertThrows(ConflictException.class, () -> departamentoService.create(input));
    }

    @Test
    void testUpdateDepartamento_Success() {
        UpdateDepartamentoInput input = new UpdateDepartamentoInput(1, "Cusco");
        Departamento dep = new Departamento();
        dep.setIdDepartamento(1);
        dep.setNombreDepartamento("Lima");

        when(departamentoRepo.findById(1)).thenReturn(Optional.of(dep));
        when(departamentoRepo.existsByNombreDepartamentoIgnoreCase("Cusco")).thenReturn(false);
        when(departamentoRepo.save(any(Departamento.class))).thenAnswer(i -> i.getArgument(0));

        var result = departamentoService.update(input);

        assertEquals("Cusco", result.nombreDepartamento());
    }

    @Test
    void testUpdateDepartamento_NotFound() {
        UpdateDepartamentoInput input = new UpdateDepartamentoInput(1, "Cusco");
        when(departamentoRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> departamentoService.update(input));
    }

    @Test
    void testFindById_Success() {
        Departamento dep = new Departamento();
        dep.setIdDepartamento(1);
        dep.setNombreDepartamento("Lima");

        when(departamentoRepo.findById(1)).thenReturn(Optional.of(dep));

        var result = departamentoService.findById(1);

        assertEquals("Lima", result.nombreDepartamento());
    }

    @Test
    void testFindById_NotFound() {
        when(departamentoRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> departamentoService.findById(1));
    }

    @Test
    void testList_Success() {
        Departamento dep1 = new Departamento();
        dep1.setIdDepartamento(1);
        dep1.setNombreDepartamento("Lima");

        Departamento dep2 = new Departamento();
        dep2.setIdDepartamento(2);
        dep2.setNombreDepartamento("Cusco");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("nombreDepartamento").ascending());
        Page<Departamento> page = new PageImpl<>(List.of(dep1, dep2), pageable, 2);

        // Aquí está la corrección importante:
        when(departamentoRepo.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<?> result = departamentoService.list(0, 10);

        assertEquals(2, result.content().size());
    }

    @Test
    void testDelete_Success() {
        when(departamentoRepo.existsById(1)).thenReturn(true);
        doNothing().when(departamentoRepo).deleteById(1);

        boolean result = departamentoService.delete(1);
        assertTrue(result);
    }

    @Test
    void testDelete_NotFound() {
        when(departamentoRepo.existsById(1)).thenReturn(false);
        boolean result = departamentoService.delete(1);
        assertFalse(result);
    }
}
