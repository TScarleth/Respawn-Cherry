package com.respawncherry.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.respawncherry.usuarios.dto.LoginRequest;
import com.respawncherry.usuarios.dto.LoginResponse;
import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.entity.Usuario;
import com.respawncherry.usuarios.exception.CredencialesInvalidasException;
import com.respawncherry.usuarios.exception.RecursoNoEncontradoException;
import com.respawncherry.usuarios.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioService usuarioService;

    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService("test-secret-respawn-cherry-32chars!", 3600000);
        authService = new AuthService(usuarioService, passwordEncoder, jwtService);
    }

    @Test
    void loginExitosoDevuelveTokenSinPassword() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Cherry");
        usuario.setCorreo("cherry@respawn.com");
        usuario.setPassword(passwordEncoder.encode("secreto123"));
        usuario.setRol(Rol.USUARIO);

        when(usuarioService.buscarPorCorreo("cherry@respawn.com")).thenReturn(usuario);

        LoginRequest request = new LoginRequest();
        request.setCorreo("cherry@respawn.com");
        request.setPassword("secreto123");

        LoginResponse respuesta = authService.login(request);

        assertNotNull(respuesta.getToken());
        assertEquals("Bearer", respuesta.getTipo());
        assertEquals("Cherry", respuesta.getUsuario().getNombre());
        assertEquals("cherry@respawn.com", respuesta.getUsuario().getCorreo());
    }

    @Test
    void loginFallaConPasswordIncorrecta() {
        Usuario usuario = new Usuario();
        usuario.setCorreo("cherry@respawn.com");
        usuario.setPassword(passwordEncoder.encode("secreto123"));
        usuario.setRol(Rol.USUARIO);
        usuario.setId(1L);
        usuario.setNombre("Cherry");

        when(usuarioService.buscarPorCorreo("cherry@respawn.com")).thenReturn(usuario);

        LoginRequest request = new LoginRequest();
        request.setCorreo("cherry@respawn.com");
        request.setPassword("otraClave");

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }

    @Test
    void loginFallaSiNoExisteUsuario() {
        when(usuarioService.buscarPorCorreo("nadie@respawn.com"))
                .thenThrow(new RecursoNoEncontradoException("Usuario no encontrado"));

        LoginRequest request = new LoginRequest();
        request.setCorreo("nadie@respawn.com");
        request.setPassword("secreto123");

        assertThrows(CredencialesInvalidasException.class, () -> authService.login(request));
    }
}
