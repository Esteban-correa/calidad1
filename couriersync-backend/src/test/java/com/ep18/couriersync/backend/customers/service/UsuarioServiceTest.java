package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.common.exception.ConflictException;
import com.ep18.couriersync.backend.common.exception.NotFoundException;
import com.ep18.couriersync.backend.customers.domain.*;
import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.*;
import com.ep18.couriersync.backend.customers.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioServiceTest {

    @InjectMocks
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepo;
    @Mock
    private CiudadRepository ciudadRepo;
    @Mock
    private DepartamentoRepository departamentoRepo;
    @Mock
    private RolRepository rolRepo;

    private Ciudad ciudad;
    private Departamento departamento;
    private Rol rol;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        departamento = new Departamento();
        departamento.setIdDepartamento(1);
        departamento.setNombreDepartamento("Departamento 1");

        ciudad = new Ciudad();
        ciudad.setIdCiudad(1);
        ciudad.setNombreCiudad("Ciudad 1");
        ciudad.setDepartamento(departamento);

        rol = new Rol();
        rol.setIdRol(1);
        rol.setNombreRol("Admin");

        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setNombre("Juan Perez");
        usuario.setCorreo("juan@example.com");
        usuario.setTelefono("1234567890");
        usuario.setFechaRegistro(LocalDate.now());
        usuario.setDetalleDireccion("Calle Falsa 123");
        usuario.setCiudad(ciudad);
        usuario.setDepartamento(departamento);
        usuario.setRol(rol);
    }

    @Test
    void testCreateUsuario_Success() {
        CreateUsuarioInput input = new CreateUsuarioInput(
                "Maria Lopez",
                "maria@example.com",
                "0987654321",
                null,
                "Direccion X",
                ciudad.getIdCiudad(),
                departamento.getIdDepartamento(),
                rol.getIdRol()
        );

        when(usuarioRepo.existsByCorreoIgnoreCase(input.correo())).thenReturn(false);
        when(ciudadRepo.findById(ciudad.getIdCiudad())).thenReturn(Optional.of(ciudad));
        when(departamentoRepo.findById(departamento.getIdDepartamento())).thenReturn(Optional.of(departamento));
        when(rolRepo.findById(rol.getIdRol())).thenReturn(Optional.of(rol));
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setIdUsuario(2);
            return u;
        });

        UsuarioView view = usuarioService.create(input);

        assertNotNull(view);
        assertEquals("Maria Lopez", view.nombre());
        assertEquals("maria@example.com", view.correo());
    }

    @Test
    void testCreateUsuario_Conflict() {
        CreateUsuarioInput input = new CreateUsuarioInput(
                "Maria Lopez",
                "juan@example.com",
                "0987654321",
                null,
                "Direccion X",
                ciudad.getIdCiudad(),
                departamento.getIdDepartamento(),
                rol.getIdRol()
        );

        when(usuarioRepo.existsByCorreoIgnoreCase(input.correo())).thenReturn(true);

        assertThrows(ConflictException.class, () -> usuarioService.create(input));
    }

    @Test
    void testFindById_Success() {
        when(usuarioRepo.findById(1)).thenReturn(Optional.of(usuario));

        UsuarioView view = usuarioService.findById(1);

        assertNotNull(view);
        assertEquals(usuario.getNombre(), view.nombre());
    }

    @Test
    void testFindById_NotFound() {
        when(usuarioRepo.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> usuarioService.findById(99));
    }

    @Test
    void testUpdateUsuario_Success() {
        UpdateUsuarioInput input = new UpdateUsuarioInput(
                1,
                "Juan Actualizado",
                "juan@example.com",
                "1111111111",
                LocalDate.now(),
                "Nueva direccion",
                null,
                null,
                null
        );

        when(usuarioRepo.findById(1)).thenReturn(Optional.of(usuario));
        when(usuarioRepo.save(any())).thenReturn(usuario);

        UsuarioView view = usuarioService.update(input);

        assertEquals("Juan Actualizado", view.nombre());
    }

    @Test
    void testDeleteUsuario_Success() {
        when(usuarioRepo.existsById(1)).thenReturn(true);
        doNothing().when(usuarioRepo).deleteById(1);

        boolean result = usuarioService.delete(1);
        assertTrue(result);
    }

    @Test
    void testDeleteUsuario_NotExists() {
        when(usuarioRepo.existsById(99)).thenReturn(false);
        boolean result = usuarioService.delete(99);
        assertFalse(result);
    }

    @Test
    void testSearchUsuarios() {
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioRepo.findByNombreContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(page);

        var result = usuarioService.search("Juan", 0, 10);
        assertEquals(1, result.content().size());
    }

    @Test
    void testListByCiudad() {
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioRepo.findAllByCiudad_IdCiudad(eq(1), any(Pageable.class))).thenReturn(page);

        var result = usuarioService.listByCiudad(1, 0, 10);
        assertEquals(1, result.content().size());
    }

    @Test
    void testListByDepartamento() {
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioRepo.findAllByDepartamento_IdDepartamento(eq(1), any(Pageable.class))).thenReturn(page);

        var result = usuarioService.listByDepartamento(1, 0, 10);
        assertEquals(1, result.content().size());
    }

    @Test
    void testListByRol() {
        Page<Usuario> page = new PageImpl<>(List.of(usuario));
        when(usuarioRepo.findAllByRol_IdRol(eq(1), any(Pageable.class))).thenReturn(page);

        var result = usuarioService.listByRol(1, 0, 10);
        assertEquals(1, result.content().size());
    }
}
