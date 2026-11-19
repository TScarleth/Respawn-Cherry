package com.respawncherry.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.entity.Usuario;
import com.respawncherry.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

class AdminBootstrapInitializerTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void creaAdminConPasswordCodificadaSoloSiNoExiste() {
        when(usuarioRepository.existsByCorreo("admin@example.test")).thenReturn(false);
        when(passwordEncoder.encode("password de prueba")).thenReturn("hash bcrypt");
        AdminBootstrapInitializer initializer = initializer("password de prueba");

        initializer.run(new DefaultApplicationArguments());

        var captor = org.mockito.ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("admin@example.test", captor.getValue().getCorreo());
        assertEquals("hash bcrypt", captor.getValue().getPassword());
        assertEquals(Rol.ADMIN, captor.getValue().getRol());
    }

    @Test
    void conservaAdminExistente() {
        when(usuarioRepository.existsByCorreo("admin@example.test")).thenReturn(true);

        initializer("password de prueba").run(new DefaultApplicationArguments());

        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any(Usuario.class));
        verify(passwordEncoder, never()).encode(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void exigePasswordCuandoElInicializadorSeEjecuta() {
        assertThrows(IllegalStateException.class,
                () -> initializer(" ").run(new DefaultApplicationArguments()));
    }

    private AdminBootstrapInitializer initializer(String password) {
        return new AdminBootstrapInitializer(
                usuarioRepository, passwordEncoder, "Admin", "admin@example.test", password);
    }
}
