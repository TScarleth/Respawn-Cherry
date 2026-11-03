package com.respawncherry.usuarios.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.entity.Usuario;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void generaYLeeToken() {
        JwtService jwtService = new JwtService("test-secret-respawn-cherry-32chars!", 3600000);
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setCorreo("cherry@respawn.com");
        usuario.setRol(Rol.ADMIN);

        String token = jwtService.generarToken(usuario);

        assertTrue(jwtService.esValido(token));
        assertEquals("cherry@respawn.com", jwtService.extraerCorreo(token));
    }

    @Test
    void secretCortoEsRechazado() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("corto", 1000));
    }
}
