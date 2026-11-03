package com.respawncherry.usuarios.service;

import com.respawncherry.usuarios.dto.RegistroRequest;
import com.respawncherry.usuarios.dto.UsuarioRequest;
import com.respawncherry.usuarios.dto.UsuarioResponse;
import com.respawncherry.usuarios.dto.UsuarioUpdateRequest;
import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.entity.Usuario;
import com.respawncherry.usuarios.exception.AccesoDenegadoException;
import com.respawncherry.usuarios.exception.CorreoYaRegistradoException;
import com.respawncherry.usuarios.exception.RecursoNoEncontradoException;
import com.respawncherry.usuarios.repository.UsuarioRepository;
import com.respawncherry.usuarios.security.UsuarioPrincipal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        validarCorreoLibre(request.getCorreo());
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.USUARIO);
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        validarCorreoLibre(request.getCorreo());
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(request.getRol());
        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id, Authentication authentication) {
        Usuario usuario = buscarPorId(id);
        validarConsultaOEdicion(usuario.getId(), authentication);
        return UsuarioMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request, Authentication authentication) {
        Usuario usuario = buscarPorId(id);
        UsuarioPrincipal actual = principal(authentication);
        validarConsultaOEdicion(usuario.getId(), authentication);

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getCorreo() != null && !request.getCorreo().isBlank()) {
            if (usuarioRepository.existsByCorreoAndIdNot(request.getCorreo(), id)) {
                throw new CorreoYaRegistradoException("El correo ya está registrado");
            }
            usuario.setCorreo(request.getCorreo());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRol() != null) {
            if (!actual.esAdmin()) {
                throw new AccesoDenegadoException("No puedes cambiar el rol");
            }
            usuario.setRol(request.getRol());
        }

        return UsuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private void validarCorreoLibre(String correo) {
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }
    }

    private void validarConsultaOEdicion(Long idObjetivo, Authentication authentication) {
        UsuarioPrincipal actual = principal(authentication);
        if (!actual.esAdmin() && !actual.getId().equals(idObjetivo)) {
            throw new AccesoDenegadoException("Solo puedes acceder a tu propia información");
        }
    }

    private UsuarioPrincipal principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioPrincipal principal)) {
            throw new AccesoDenegadoException("No autenticado");
        }
        return principal;
    }
}
