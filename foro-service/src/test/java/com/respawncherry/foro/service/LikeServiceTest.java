package com.respawncherry.foro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.entity.PublicacionLike;
import com.respawncherry.foro.exception.SolicitudInvalidaException;
import com.respawncherry.foro.repository.PublicacionLikeRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class LikeServiceTest {

    private final PublicacionRepository publicaciones = mock(PublicacionRepository.class);
    private final PublicacionLikeRepository likes = mock(PublicacionLikeRepository.class);
    private final LikeService service = new LikeService(publicaciones, likes);

    @Test
    void quitaElLikeSiYaExistia() {
        Publicacion publicacion = mock(Publicacion.class);
        PublicacionLike like = new PublicacionLike(publicacion, 4L);
        when(publicaciones.findById(8L)).thenReturn(Optional.of(publicacion));
        when(likes.findByPublicacion_IdAndUsuarioId(8L, 4L)).thenReturn(Optional.of(like), Optional.empty());
        when(likes.countByPublicacion_Id(8L)).thenReturn(2L);

        var respuesta = service.alternar(8L, auth(4L));

        verify(likes).delete(like);
        assertFalse(respuesta.meGusta());
        assertEquals(2L, respuesta.likes());
    }

    @Test
    void rechazaUnArchivoQueNoEsImagen() {
        assertThrows(SolicitudInvalidaException.class, () -> ImagenService.tipoSeguro(new byte[] {1, 2, 3, 4}));
    }

    @Test
    void reconocePng() {
        byte[] png = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0, 0, 0, 0, 0};
        assertEquals("image/png", ImagenService.tipoSeguro(png));
        assertTrue(png.length > 4);
    }

    private UsernamePasswordAuthenticationToken auth(long id) {
        ForoPrincipal principal = new ForoPrincipal(id, "ana@example.com", "USUARIO");
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}
