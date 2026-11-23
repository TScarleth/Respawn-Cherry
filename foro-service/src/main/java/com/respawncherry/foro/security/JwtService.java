package com.respawncherry.foro.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        secret = secret == null ? "" : secret.trim();
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        key = Keys.hmacShaKeyFor(bytes);
    }

    private static long idDe(Object claim) {
        if (claim instanceof Number number) {
            return number.longValue();
        }
        if (claim instanceof String texto) {
            try {
                return Long.parseLong(texto.trim());
            } catch (NumberFormatException ex) {
                return -1;
            }
        }
        return -1;
    }

    public ForoPrincipal validar(String token) {
        Claims claims = Jwts.parser().verifyWith(key).clockSkewSeconds(60).build()
                .parseSignedClaims(token).getPayload();
        long id = idDe(claims.get("userId"));
        String rol = claims.get("rol", String.class);
        if (id < 1 || claims.getSubject() == null || claims.getSubject().isBlank()
                || !List.of("ADMIN", "USUARIO").contains(rol)) {
            throw new IllegalArgumentException("Claims de usuario inválidos");
        }
        return new ForoPrincipal(id, claims.getSubject(), rol);
    }
}
