package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.common.exception.ConflictException;
import com.ep18.couriersync.backend.common.exception.NotFoundException;
import com.ep18.couriersync.backend.customers.domain.Ciudad;
import com.ep18.couriersync.backend.customers.domain.Departamento;
import com.ep18.couriersync.backend.customers.dto.CiudadDTOs.CreateCiudadInput;
import com.ep18.couriersync.backend.customers.dto.CiudadDTOs.UpdateCiudadInput;
import com.ep18.couriersync.backend.customers.dto.CiudadDTOs.CiudadView;
import com.ep18.couriersync.backend.customers.repository.CiudadRepository;
import com.ep18.couriersync.backend.customers.repository.DepartamentoRepository;
import com.ep18.couriersync.backend.common.dto.PagingDTOs.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CiudadServiceTest {

    @Mock
    private CiudadRepository ciudadRepo;

    @Mock
    private DepartamentoRepository departamentoRepo;

    @InjectMocks
    private CiudadService ciudadService;

    private Departamento depto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        depto = new Departamento();
        depto.setIdDepartamento(1);
        depto.setNombreDepartamento("Lima");
    }

    @Test
    void testCreateCiudad_Success() {
        CreateCiudadInput input = new CreateCiudadInput("Miraflores", 1);

        when(departamentoRepo.findById(1)).thenReturn(Optional.of(depto));
        when(ciudadRepo.existsByNombreCiudadIgnoreCaseAndDepartamento_IdDepartamento("Miraflores", 1))
                .thenReturn(false);
        Ciudad saved = new Ciudad();
        saved.setIdCiudad(100);
        saved.setNombreCiudad("Miraflores");
        saved.setDepartamento(depto);
        when(ciudadRepo.save(any(Ciudad.class))).thenReturn(saved);

        CiudadView result = ciudadService.create(input);

        assertEquals("Miraflores", result.nombreCiudad());
        assertEquals(1, result.idDepartamento());
    }

    @Test
    void testCreateCiudad_DepartamentoNotFound() {
        CreateCiudadInput input = new CreateCiudadInput("Miraflores", 2);
        when(departamentoRepo.findById(2)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> ciudadService.create(input));
    }

    @Test
    void testUpdateCiudad_Success() {
        Ciudad existing = new Ciudad();
        existing.setIdCiudad(100);
        existing.setNombreCiudad("San Isidro");
        existing.setDepartamento(depto);

        UpdateCiudadInput input = new UpdateCiudadInput(100, "Miraflores", null);

        when(ciudadRepo.findById(100)).thenReturn(Optional.of(existing));
        when(ciudadRepo.existsByNombreCiudadIgnoreCaseAndDepartamento_IdDepartamento("Miraflores", 1))
                .thenReturn(false);
        when(ciudadRepo.save(any(Ciudad.class))).thenAnswer(invocation -> invocation.getArgument(0)); // devolver el objeto actualizado

        CiudadView result = ciudadService.update(input);

        assertEquals("Miraflores", result.nombreCiudad()); // ahora coincide con la actualización
    }


    @Test
    void testFindById_NotFound() {
        when(ciudadRepo.findById(999)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> ciudadService.findById(999));
    }

    @Test
    void testListByDepartamento() {
        Ciudad c1 = new Ciudad();
        c1.setIdCiudad(1);
        c1.setNombreCiudad("Miraflores");
        c1.setDepartamento(depto);

        Page<Ciudad> page = new PageImpl<>(List.of(c1));
        when(ciudadRepo.findAllByDepartamento_IdDepartamento(eq(1), any())).thenReturn(page);

        PageResponse<?> response = ciudadService.listByDepartamento(1, 0, 10);

        assertEquals(1, response.content().size());
        CiudadView view = (CiudadView) response.content().get(0);
        assertEquals("Miraflores", view.nombreCiudad());
    }

    @Test
    void testSearch() {
        Ciudad c1 = new Ciudad();
        c1.setIdCiudad(1);
        c1.setNombreCiudad("Miraflores");
        c1.setDepartamento(depto);

        Page<Ciudad> page = new PageImpl<>(List.of(c1));
        when(ciudadRepo.findByNombreCiudadContainingIgnoreCase(eq("Mira"), any())).thenReturn(page);

        PageResponse<?> response = ciudadService.search("Mira", 0, 10);

        assertEquals(1, response.content().size());
        CiudadView view = (CiudadView) response.content().get(0);
        assertEquals("Miraflores", view.nombreCiudad());
    }
}
