package com.respawncherry.foro.security;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.respawncherry.foro.repository.CategoriaRepository;
import com.respawncherry.foro.repository.ComentarioRepository;
import com.respawncherry.foro.repository.DenunciaRepository;
import com.respawncherry.foro.repository.PublicacionImagenRepository;
import com.respawncherry.foro.repository.PublicacionLikeRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "jwt.secret=respawn-cherry-test-secret-0123456789-abcdef",
        "foro.categorias.seed=false"
})
class JwtWebTest {

    private static final String SECRET = "respawn-cherry-test-secret-0123456789-abcdef";

    @LocalServerPort
    private int port;

    @MockitoBean
    private CategoriaRepository categoriaRepository;

    @MockitoBean
    private PublicacionRepository publicacionRepository;

    @MockitoBean
    private ComentarioRepository comentarioRepository;

    @MockitoBean
    private PublicacionLikeRepository publicacionLikeRepository;

    @MockitoBean
    private DenunciaRepository denunciaRepository;

    @MockitoBean
    private PublicacionImagenRepository publicacionImagenRepository;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @Test
    void unLikeConElTokenDeUsuariosNoQuedaAnonimo() {
        String token = Jwts.builder()
                .subject("gamer@example.com")
                .claim("userId", 42L)
                .claim("rol", "USUARIO")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        HttpStatusCode status = RestClient.create()
                .post()
                .uri("http://127.0.0.1:" + port + "/api/publicaciones/1/likes")
                .header("Authorization", "Bearer " + token)
                .header("Origin", "http://127.0.0.1:4200")
                .retrieve()
                .onStatus(code -> true, (request, response) -> {})
                .toBodilessEntity()
                .getStatusCode();

        assertEquals(404, status.value());
    }
}
