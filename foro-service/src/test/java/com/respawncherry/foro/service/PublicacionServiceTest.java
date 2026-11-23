package com.respawncherry.foro.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.exception.AccesoDenegadoException;
import com.respawncherry.foro.repository.CategoriaRepository;
import com.respawncherry.foro.repository.PublicacionImagenRepository;
import com.respawncherry.foro.repository.PublicacionLikeRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class PublicacionServiceTest {

    private final PublicacionRepository publicacionRepository = mock(PublicacionRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final PublicacionLikeRepository likeRepository = mock(PublicacionLikeRepository.class);
    private final PublicacionImagenRepository imagenRepository = mock(PublicacionImagenRepository.class);
    private final PublicacionService service =
            new PublicacionService(publicacionRepository, categoriaRepository, likeRepository, imagenRepository);

    @Test
    void filtraPorTituloYCategoria() {
        when(publicacionRepository.buscar("zelda", 3L)).thenReturn(List.of());

        assertEquals(List.of(), service.listar(" zelda ", 3L, null));
        verify(publicacionRepository).buscar("zelda", 3L);
    }

    @Test
    void usuarioNoPuedeEditarPublicacionAjena() {
        Publicacion publicacion = mock(Publicacion.class);
        when(publicacion.getAutorId()).thenReturn(7L);
        when(publicacionRepository.findById(12L)).thenReturn(Optional.of(publicacion));
        ForoPrincipal principal = new ForoPrincipal(9L, "usuario@example.com", "USUARIO");
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());

        assertThrows(AccesoDenegadoException.class,
                () -> service.actualizar(12L, null, authentication));
        verify(categoriaRepository, never()).findById(1L);
    }
}
