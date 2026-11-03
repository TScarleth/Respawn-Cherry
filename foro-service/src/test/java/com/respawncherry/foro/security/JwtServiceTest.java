package com.respawncherry.foro.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "respawn-cherry-test-secret-0123456789-abcdef";

    private final JwtService jwtService = new JwtService(SECRET);

    @Test
    void validaLosClaimsCompartidosConUsuariosService() {
        String token = Jwts.builder()
                .subject("gamer@example.com")
                .claim("userId", 42L)
                .claim("rol", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        ForoPrincipal principal = jwtService.validar(token);

        assertEquals(42L, principal.getId());
        assertEquals("gamer@example.com", principal.getUsername());
        assertTrue(principal.esAdmin());
    }

    @Test
    void rechazaTokensConOtraFirma() {
        String token = Jwts.builder()
                .subject("gamer@example.com")
                .claim("userId", 42L)
                .claim("rol", "USUARIO")
                .signWith(Keys.hmacShaKeyFor("otra-clave-larga-pero-distinta-123456".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(RuntimeException.class, () -> jwtService.validar(token));
    }
}
