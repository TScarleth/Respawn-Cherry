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
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        key = Keys.hmacShaKeyFor(bytes);
    }

    public ForoPrincipal validar(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        Object idClaim = claims.get("userId");
        String rol = claims.get("rol", String.class);
        if (!(idClaim instanceof Number id) || claims.getSubject() == null
                || !List.of("ADMIN", "USUARIO").contains(rol)) {
            throw new IllegalArgumentException("Claims de usuario inválidos");
        }
        return new ForoPrincipal(id.longValue(), claims.getSubject(), rol);
    }
}
