package com.respawncherry.foro;

import com.respawncherry.foro.repository.CategoriaRepository;
import com.respawncherry.foro.repository.ComentarioRepository;
import com.respawncherry.foro.repository.DenunciaRepository;
import com.respawncherry.foro.repository.PublicacionImagenRepository;
import com.respawncherry.foro.repository.PublicacionLikeRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "jwt.secret=respawn-cherry-test-secret-0123456789-abcdef",
        "foro.categorias.seed=false"
})
class ForoServiceApplicationTests {

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
    void contextLoads() {
    }
}
