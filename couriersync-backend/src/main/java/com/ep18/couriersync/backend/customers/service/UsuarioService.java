package com.ep18.couriersync.backend.customers.service;

import com.ep18.couriersync.backend.common.dto.PagingDTOs.PageResponse;
import com.ep18.couriersync.backend.common.exception.ConflictException;
import com.ep18.couriersync.backend.common.exception.NotFoundException;
import com.ep18.couriersync.backend.common.pagination.PageMapper;
import com.ep18.couriersync.backend.common.pagination.PageRequestUtil;
import com.ep18.couriersync.backend.customers.domain.*;
import com.ep18.couriersync.backend.customers.dto.UsuarioDTOs.*;
import com.ep18.couriersync.backend.customers.repository.*;
import com.ep18.couriersync.backend.customers.validator.UsuarioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.function.Supplier;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final CiudadRepository ciudadRepo;
    private final DepartamentoRepository departamentoRepo;
    private final RolRepository rolRepo;

    // ---------- CREATE ----------

    @Transactional
    public UsuarioView create(CreateUsuarioInput in) {
        validarCorreoUnico(in.correo());
        Ciudad ciudad = obtenerCiudad(in.idCiudad());
        Departamento depto = obtenerDepartamento(in.idDepartamento());
        Rol rol = obtenerRol(in.idRol());

        UsuarioValidator.assertCiudadPerteneceADepartamento(ciudad, depto.getIdDepartamento());

        Usuario u = construirUsuario(in, ciudad, depto, rol);
        return toView(usuarioRepo.save(u));
    }

    // ---------- UPDATE ----------

    @Transactional
    public UsuarioView update(UpdateUsuarioInput in) {
        Usuario u = obtenerUsuario(in.idUsuario());
        actualizarCorreo(in, u);
        actualizarCamposBasicos(in, u);
        actualizarRelaciones(in, u);
        return toView(usuarioRepo.save(u));
    }

    // ---------- READ ----------

    @Transactional(readOnly = true)
    public UsuarioView findById(Integer id) {
        return usuarioRepo.findById(id)
                .map(this::toView)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> search(String q, Integer page, Integer size) {
        Page<Usuario> p = usuarioRepo.findByNombreContainingIgnoreCase(
                q == null ? "" : q, PageRequestUtil.of(page, size, Sort.by("nombre")));
        return PageMapper.map(p, this::toView);
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> listByCiudad(Integer idCiudad, Integer page, Integer size) {
        return listarUsuarios(() -> usuarioRepo.findAllByCiudad_IdCiudad(
                idCiudad, PageRequestUtil.of(page, size, Sort.by("nombre"))));
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> listByDepartamento(Integer idDepto, Integer page, Integer size) {
        return listarUsuarios(() -> usuarioRepo.findAllByDepartamento_IdDepartamento(
                idDepto, PageRequestUtil.of(page, size, Sort.by("nombre"))));
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioView> listByRol(Integer idRol, Integer page, Integer size) {
        return listarUsuarios(() -> usuarioRepo.findAllByRol_IdRol(
                idRol, PageRequestUtil.of(page, size, Sort.by("nombre"))));
    }

    // ---------- DELETE ----------

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

    // ---------- PRIVADOS / HELPERS ----------

    private void validarCorreoUnico(String correo) {
        if (usuarioRepo.existsByCorreoIgnoreCase(correo))
            throw new ConflictException("El correo ya está registrado");
    }

    private Usuario obtenerUsuario(Integer id) {
        return usuarioRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private Ciudad obtenerCiudad(Integer id) {
        return ciudadRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ciudad no encontrada"));
    }

    private Departamento obtenerDepartamento(Integer id) {
        return departamentoRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Departamento no encontrado"));
    }

    private Rol obtenerRol(Integer id) {
        return rolRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));
    }

    private Usuario construirUsuario(CreateUsuarioInput in, Ciudad c, Departamento d, Rol r) {
        Usuario u = new Usuario();
        u.setNombre(in.nombre());
        u.setCorreo(in.correo());
        u.setTelefono(in.telefono());
        u.setFechaRegistro(in.fechaRegistro() != null ? in.fechaRegistro() : LocalDate.now());
        u.setDetalleDireccion(in.detalleDireccion());
        u.setCiudad(c);
        u.setDepartamento(d);
        u.setRol(r);
        return u;
    }

    private void actualizarCorreo(UpdateUsuarioInput in, Usuario u) {
        if (in.correo() != null && !in.correo().equalsIgnoreCase(u.getCorreo()))
            validarCorreoUnico(in.correo());
    }

    private void actualizarCamposBasicos(UpdateUsuarioInput in, Usuario u) {
        if (in.nombre() != null) u.setNombre(in.nombre());
        if (in.correo() != null) u.setCorreo(in.correo());
        if (in.telefono() != null) u.setTelefono(in.telefono());
        if (in.fechaRegistro() != null) u.setFechaRegistro(in.fechaRegistro());
        if (in.detalleDireccion() != null) u.setDetalleDireccion(in.detalleDireccion());
    }

    private void actualizarRelaciones(UpdateUsuarioInput in, Usuario u) {
        if (in.idCiudad() != null || in.idDepartamento() != null) {
            Ciudad c = in.idCiudad() != null ? obtenerCiudad(in.idCiudad()) : u.getCiudad();
            Departamento d = in.idDepartamento() != null ? obtenerDepartamento(in.idDepartamento()) : u.getDepartamento();
            UsuarioValidator.assertCiudadPerteneceADepartamento(c, d.getIdDepartamento());
            u.setCiudad(c);
            u.setDepartamento(d);
        }
        if (in.idRol() != null) u.setRol(obtenerRol(in.idRol()));
    }

    private PageResponse<UsuarioView> listarUsuarios(Supplier<Page<Usuario>> pageSupplier) {
        return PageMapper.map(pageSupplier.get(), this::toView);
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
