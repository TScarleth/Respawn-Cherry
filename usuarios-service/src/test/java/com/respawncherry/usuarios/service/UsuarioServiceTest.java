package com.respawncherry.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private PasswordEncoder passwordEncoder;
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void registrarCifraPasswordYAsignaRolUsuario() {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Cherry");
        request.setCorreo("cherry@respawn.com");
        request.setPassword("secreto123");

        when(usuarioRepository.existsByCorreo("cherry@respawn.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponse respuesta = usuarioService.registrar(request);

        assertEquals(1L, respuesta.getId());
        assertEquals("Cherry", respuesta.getNombre());
        assertEquals("cherry@respawn.com", respuesta.getCorreo());
        assertEquals(Rol.USUARIO, respuesta.getRol());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void registrarRechazaCorreoDuplicado() {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Cherry");
        request.setCorreo("cherry@respawn.com");
        request.setPassword("secreto123");
        when(usuarioRepository.existsByCorreo("cherry@respawn.com")).thenReturn(true);

        assertThrows(CorreoYaRegistradoException.class, () -> usuarioService.registrar(request));
    }

    @Test
    void crearGuardaHashBcrypt() {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Admin");
        request.setCorreo("admin@respawn.com");
        request.setPassword("admin1234");
        request.setRol(Rol.ADMIN);

        when(usuarioRepository.existsByCorreo("admin@respawn.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            assertTrue(passwordEncoder.matches("admin1234", usuario.getPassword()));
            assertFalse(usuario.getPassword().contains("admin1234"));
            usuario.setId(2L);
            return usuario;
        });

        UsuarioResponse respuesta = usuarioService.crear(request);
        assertEquals(Rol.ADMIN, respuesta.getRol());
    }

    @Test
    void usuarioNoPuedeVerAOtro() {
        Usuario objetivo = usuario(10L, Rol.USUARIO, "otro@respawn.com");
        Usuario actual = usuario(3L, Rol.USUARIO, "yo@respawn.com");
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(objetivo));

        assertThrows(AccesoDenegadoException.class, () -> usuarioService.obtener(10L, auth(actual)));
    }

    @Test
    void adminPuedeVerCualquierUsuario() {
        Usuario objetivo = usuario(10L, Rol.USUARIO, "otro@respawn.com");
        Usuario admin = usuario(1L, Rol.ADMIN, "admin@respawn.com");
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(objetivo));

        UsuarioResponse respuesta = usuarioService.obtener(10L, auth(admin));
        assertEquals("otro@respawn.com", respuesta.getCorreo());
    }

    @Test
    void usuarioNoPuedeCambiarRol() {
        Usuario actual = usuario(3L, Rol.USUARIO, "yo@respawn.com");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(actual));

        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setRol(Rol.ADMIN);

        assertThrows(AccesoDenegadoException.class, () -> usuarioService.actualizar(3L, request, auth(actual)));
    }

    @Test
    void eliminarUsuarioInexistenteLanzaError() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);
        assertThrows(RecursoNoEncontradoException.class, () -> usuarioService.eliminar(99L));
    }

    @Test
    void listarNoIncluyePassword() {
        Usuario usuario = usuario(1L, Rol.USUARIO, "a@respawn.com");
        usuario.setPassword("$2a$hash");
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        UsuarioResponse respuesta = usuarioService.listar().get(0);
        assertEquals("a@respawn.com", respuesta.getCorreo());
        assertEquals("Neko", respuesta.getNombre());
    }

    private Usuario usuario(Long id, Rol rol, String correo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Neko");
        usuario.setCorreo(correo);
        usuario.setPassword("$2a$10$hash");
        usuario.setRol(rol);
        return usuario;
    }

    private Authentication auth(Usuario usuario) {
        UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
