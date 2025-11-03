package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.common.dto.PagingDTOs.PageResponse;
import com.ep18.couriersync.backend.common.exception.ConflictException;
import com.ep18.couriersync.backend.common.exception.NotFoundException;
import com.ep18.couriersync.backend.common.pagination.PageMapper;
import com.ep18.couriersync.backend.common.pagination.PageRequestUtil;
import com.ep18.couriersync.backend.customers.domain.Ciudad;
import com.ep18.couriersync.backend.customers.domain.Departamento;
import com.ep18.couriersync.backend.customers.domain.Rol;
import com.ep18.couriersync.backend.customers.domain.Usuario;
import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.CreateUsuarioInput;
import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.UpdateUsuarioInput;
import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.UsuarioView;
import com.ep18.couriersync.backend.customers.repository.CiudadRepository;
import com.ep18.couriersync.backend.customers.repository.DepartamentoRepository;
import com.ep18.couriersync.backend.customers.repository.RolRepository;
import com.ep18.couriersync.backend.customers.repository.UsuarioRepository;
import com.ep18.couriersync.backend.customers.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    // --- Repositorios (Inyectados por @RequiredArgsConstructor) ---
    private final UsuarioRepository usuarioRepo;
    private final CiudadRepository ciudadRepo;
    private final DepartamentoRepository departamentoRepo;
    private final RolRepository rolRepo;

    // --- Métodos Públicos (API) ---

    @Transactional
    public UsuarioView create(CreateUsuarioInput in) {
        // 1. Validar unicidad
        validateNewEmail(in.correo());

        // 2. Obtener y validar relaciones (FKs)
        Ciudad ciudad = fetchCiudad(in.idCiudad());
        Departamento depto = fetchDepartamento(in.idDepartamento());
        Rol rol = fetchRol(in.idRol());

        // 3. Validar lógica de negocio
        UsuarioValidator.assertCiudadPerteneceADepartamento(ciudad, depto.getIdDepartamento());

        // 4. Mapear y guardar
        Usuario u = new Usuario();
        mapCreateInputToEntity(u, in, ciudad, depto, rol);

        return toView(usuarioRepo.save(u));
    }

    @Transactional
    public UsuarioView update(UpdateUsuarioInput in) {
        // 1. Obtener entidad
        Usuario u = fetchUsuario(in.idUsuario());

        // 2. Validar cambios (si los hay)
        validateUpdatedEmail(in.correo(), u.getCorreo());

        // 3. Mapear campos simples
        mapSimpleUpdateFields(u, in);

        // 4. Mapear y validar relaciones (lógica más compleja)
        mapRelationshipUpdateFields(u, in);

        // 5. Guardar y devolver
        return toView(usuarioRepo.save(u));
    }

    @Transactional(readOnly = true)
    public UsuarioView findById(Integer id) {
        return usuarioRepo.findById(id).map(this::toView)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }
    
    // [.. Los otros métodos 'search' y 'listBy' siguen igual ..]
    // ... (search, listByCiudad, listByDepartamento, listByRol) ...

    @Transactional
    public boolean delete(Integer id) {
        if (!usuarioRepo.existsById(id)) return false;
        try {
            usuarioRepo.deleteById(id);
            return true;
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("No se puede eliminar: existen registros relacionados");
        }
    }
    
    // --- Métodos Privados de Ayuda (Helpers) ---

    /**
     * Mapea el DTO de creación a la entidad.
     */
    private void mapCreateInputToEntity(Usuario u, CreateUsuarioInput in, Ciudad ciudad, Departamento depto, Rol rol) {
        u.setNombre(in.nombre());
        u.setCorreo(in.correo());
        u.setTelefono(in.telefono());
        u.setFechaRegistro(in.fechaRegistro() != null ? in.fechaRegistro() : LocalDate.now());
        u.setDetalleDireccion(in.detalleDireccion());
        u.setCiudad(ciudad);
        u.setDepartamento(depto);
        u.setRol(rol);
    }

    /**
     * Mapea solo los campos simples de un DTO de actualización a la entidad.
     */
    private void mapSimpleUpdateFields(Usuario u, UpdateUsuarioInput in) {
        if (in.nombre() != null) u.setNombre(in.nombre());
        if (in.correo() != null) u.setCorreo(in.correo());
        if (in.telefono() != null) u.setTelefono(in.telefono());
        if (in.fechaRegistro() != null) u.setFechaRegistro(in.fechaRegistro());
        if (in.detalleDireccion() != null) u.setDetalleDireccion(in.detalleDireccion());
    }

    /**
     * Mapea y valida las relaciones (FKs) de un DTO de actualización.
     */
    private void mapRelationshipUpdateFields(Usuario u, UpdateUsuarioInput in) {
        // Determina la ciudad y depto finales (nuevos o los existentes)
        Ciudad ciudadFinal = (in.idCiudad() != null)
                ? fetchCiudad(in.idCiudad())
                : u.getCiudad();
        
        Departamento deptoFinal = (in.idDepartamento() != null)
                ? fetchDepartamento(in.idDepartamento())
                : u.getDepartamento();

        // Si alguna de las dos relaciones cambió, se debe re-validar
        if (in.idCiudad() != null || in.idDepartamento() != null) {
            UsuarioValidator.assertCiudadPerteneceADepartamento(ciudadFinal, deptoFinal.getIdDepartamento());
            u.setCiudad(ciudadFinal);
            u.setDepartamento(deptoFinal);
        }

        // Actualiza el Rol si se proporcionó uno nuevo
        if (in.idRol() != null) {
            u.setRol(fetchRol(in.idRol()));
        }
    }

    /**
     * Valida si un nuevo correo ya existe.
     */
    private void validateNewEmail(String correo) {
        if (correo != null && usuarioRepo.existsByCorreoIgnoreCase(correo)) {
            throw new ConflictException("El correo ya está registrado");
        }
    }

    /**
     * Valida si el correo se cambió a uno que ya existe.
     */
    private void validateUpdatedEmail(String nuevoCorreo, String correoActual) {
        if (nuevoCorreo == null || nuevoCorreo.equalsIgnoreCase(correoActual)) {
            return; // No hay cambio o es el mismo, no se valida
        }
        validateNewEmail(nuevoCorreo); // Valida el nuevo correo
    }

    // --- Métodos Privados de Búsqueda (Fetchers) ---

    private Usuario fetchUsuario(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private Ciudad fetchCiudad(Integer id) {
        return ciudadRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ciudad no encontrada"));
    }

    private Departamento fetchDepartamento(Integer id) {
        return departamentoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Departamento no encontrado"));
    }

    private Rol fetchRol(Integer id) {
        return rolRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
    }

    /**
     * Convierte una entidad Usuario a su DTO de vista (UsuarioView).
     * Esta versión es más segura contra NullPointerExceptions.
     */
    private UsuarioView toView(Usuario u) {
        // Extrae los objetos relacionados de forma segura
        Ciudad c = u.getCiudad();
        Departamento d = u.getDepartamento();
        Rol r = u.getRol();

        // Asigna valores o null si el objeto relacionado no está presente
        Integer idCiudad = (c != null) ? c.getIdCiudad() : null;
        String nombreCiudad = (c != null) ? c.getNombreCiudad() : null;
        
        Integer idDepto = (d != null) ? d.getIdDepartamento() : null;
        String nombreDepto = (d != null) ? d.getNombreDepartamento() : null;

        Integer idRol = (r != null) ? r.getIdRol() : null;
        String nombreRol = (r != null) ? r.getNombreRol() : null;

        return new UsuarioView(
                u.getIdUsuario(),
                u.getNombre(),
                u.getCorreo(),
                u.getTelefono(),
                u.getFechaRegistro(),
                u.getDetalleDireccion(),
                idCiudad,
                nombreCiudad,
                idDepto,
                nombreDepto,
                idRol,
                nombreRol
        );
    }

    // --- Los métodos de paginación no los he tocado ---
    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> search(String q, Integer page, Integer size) {
        Page<Usuario> p = usuarioRepo.findByNombreContainingIgnoreCase(
                (q == null ? "" : q), PageRequestUtil.of(page, size, Sort.by("nombre").ascending()));
        return PageMapper.map(p, this::toView);
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> listByCiudad(Integer idCiudad, Integer page, Integer size) {
        Page<Usuario> p = usuarioRepo.findAllByCiudad_IdCiudad(
                idCiudad, PageRequestUtil.of(page, size, Sort.by("nombre").ascending()));
        return PageMapper.map(p, this::toView);
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> listByDepartamento(Integer idDepto, Integer page, Integer size) {
        Page<Usuario> p = usuarioRepo.findAllByDepartamento_IdDepartamento(
                idDepto, PageRequestUtil.of(page, size, Sort.by("nombre").ascending()));
        return PageMapper.map(p, this::toView);
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> listByRol(Integer idRol, Integer page, Integer size) {
        Page<Usuario> p = usuarioRepo.findAllByRol_IdRol(
                idRol, PageRequestUtil.of(page, size, Sort.by("nombre").ascending()));
        return PageMapper.map(p, this::toView);
    }
}