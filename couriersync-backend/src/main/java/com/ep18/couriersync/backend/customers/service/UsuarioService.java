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

    private final UsuarioRepository usuarioRepo;
    private final CiudadRepository ciudadRepo;
    private final DepartamentoRepository departamentoRepo;
    private final RolRepository rolRepo;

    // -------------------------------
    // CREATE
    // -------------------------------
    @Transactional
    public UsuarioView create(CreateUsuarioInput in) {
        validarCorreoUnico(in.correo(), null);

        Ciudad ciudad = getCiudad(in.idCiudad());
        Departamento depto = getDepartamento(in.idDepartamento());
        Rol rol = getRol(in.idRol());

        UsuarioValidator.assertCiudadPerteneceADepartamento(ciudad, depto.getIdDepartamento());

        Usuario u = mapToUsuario(in, ciudad, depto, rol);

        return toView(usuarioRepo.save(u));
    }

    // -------------------------------
    // UPDATE
    // -------------------------------
    @Transactional
    public UsuarioView update(UpdateUsuarioInput in) {
        Usuario u = usuarioRepo.findById(in.idUsuario())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        validarCorreoUnico(in.correo(), u.getCorreo());

        actualizarCamposSimples(u, in);
        actualizarRelaciones(u, in);

        return toView(usuarioRepo.save(u));
    }

    // -------------------------------
    // READ
    // -------------------------------
    @Transactional(readOnly = true)
    public UsuarioView findById(Integer id) {
        return usuarioRepo.findById(id).map(this::toView)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

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

    // -------------------------------
    // DELETE
    // -------------------------------
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

    // -------------------------------
    // HELPERS
    // -------------------------------
    private void validarCorreoUnico(String nuevoCorreo, String correoActual) {
        if (nuevoCorreo != null && !nuevoCorreo.equalsIgnoreCase(correoActual)
                && usuarioRepo.existsByCorreoIgnoreCase(nuevoCorreo)) {
            throw new ConflictException("El correo ya está registrado");
        }
    }

    private Ciudad getCiudad(Integer idCiudad) {
        return ciudadRepo.findById(idCiudad)
                .orElseThrow(() -> new NotFoundException("Ciudad no encontrada"));
    }

    private Departamento getDepartamento(Integer idDepto) {
        return departamentoRepo.findById(idDepto)
                .orElseThrow(() -> new NotFoundException("Departamento no encontrado"));
    }

    private Rol getRol(Integer idRol) {
        return rolRepo.findById(idRol)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
    }

    private Usuario mapToUsuario(CreateUsuarioInput in, Ciudad ciudad, Departamento depto, Rol rol) {
        Usuario u = new Usuario();
        u.setNombre(in.nombre());
        u.setCorreo(in.correo());
        u.setTelefono(in.telefono());
        u.setFechaRegistro(in.fechaRegistro() != null ? in.fechaRegistro() : LocalDate.now());
        u.setDetalleDireccion(in.detalleDireccion());
        u.setCiudad(ciudad);
        u.setDepartamento(depto);
        u.setRol(rol);
        return u;
    }

    private void actualizarCamposSimples(Usuario u, UpdateUsuarioInput in) {
        if (in.nombre() != null) u.setNombre(in.nombre());
        if (in.correo() != null) u.setCorreo(in.correo());
        if (in.telefono() != null) u.setTelefono(in.telefono());
        if (in.fechaRegistro() != null) u.setFechaRegistro(in.fechaRegistro());
        if (in.detalleDireccion() != null) u.setDetalleDireccion(in.detalleDireccion());
    }

    private void actualizarRelaciones(Usuario u, UpdateUsuarioInput in) {
        if (in.idCiudad() != null || in.idDepartamento() != null) {
            Ciudad ciudad = (in.idCiudad() != null) ? getCiudad(in.idCiudad()) : u.getCiudad();
            Departamento depto = (in.idDepartamento() != null) ? getDepartamento(in.idDepartamento()) : u.getDepartamento();
            UsuarioValidator.assertCiudadPerteneceADepartamento(ciudad, depto.getIdDepartamento());
            u.setCiudad(ciudad);
            u.setDepartamento(depto);
        }

        if (in.idRol() != null) {
            u.setRol(getRol(in.idRol()));
        }
    }

    private UsuarioView toView(Usuario u) {
        return new UsuarioView(
                u.getIdUsuario(),
                u.getNombre(),
                u.getCorreo(),
                u.getTelefono(),
                u.getFechaRegistro(),
                u.getDetalleDireccion(),
                u.getCiudad().getIdCiudad(),
                u.getCiudad().getNombreCiudad(),
                u.getDepartamento().getIdDepartamento(),
                u.getDepartamento().getNombreDepartamento(),
                u.getRol().getIdRol(),
                u.getRol().getNombreRol()
        );
    }
}
