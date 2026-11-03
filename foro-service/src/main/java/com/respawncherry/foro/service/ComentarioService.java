package com.respawncherry.foro.service;

import com.respawncherry.foro.dto.ComentarioRequest;
import com.respawncherry.foro.dto.ComentarioResponse;
import com.respawncherry.foro.entity.Comentario;
import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.exception.AccesoDenegadoException;
import com.respawncherry.foro.exception.RecursoNoEncontradoException;
import com.respawncherry.foro.repository.ComentarioRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository, PublicacionRepository publicacionRepository) {
        this.comentarioRepository = comentarioRepository;
        this.publicacionRepository = publicacionRepository;
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar() {
        return comentarioRepository.findAll().stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorPublicacion(Long publicacionId) {
        obtenerPublicacion(publicacionId);
        return comentarioRepository.findByPublicacion_IdOrderByFechaCreacionAsc(publicacionId)
                .stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public ComentarioResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    @Transactional
    public ComentarioResponse crear(Long publicacionId, ComentarioRequest request, Authentication authentication) {
        Publicacion publicacion = obtenerPublicacion(publicacionId);
        ForoPrincipal autor = principal(authentication);
        return respuesta(comentarioRepository.save(
                new Comentario(request.contenido().trim(), publicacion, autor.getId())));
    }

    @Transactional
    public ComentarioResponse actualizar(Long id, ComentarioRequest request, Authentication authentication) {
        Comentario comentario = buscar(id);
        autorizar(comentario.getAutorId(), authentication);
        comentario.setContenido(request.contenido().trim());
        return respuesta(comentarioRepository.save(comentario));
    }

    @Transactional
    public void eliminar(Long id, Authentication authentication) {
        Comentario comentario = buscar(id);
        autorizar(comentario.getAutorId(), authentication);
        comentarioRepository.delete(comentario);
    }

    private Publicacion obtenerPublicacion(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Publicación no encontrada"));
    }

    private Comentario buscar(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comentario no encontrado"));
    }

    private ComentarioResponse respuesta(Comentario c) {
        return new ComentarioResponse(
                c.getId(), c.getContenido(), c.getPublicacion().getId(), c.getAutorId(), c.getFechaCreacion());
    }

    private void autorizar(Long autorId, Authentication authentication) {
        ForoPrincipal principal = principal(authentication);
        if (!principal.esAdmin() && !autorId.equals(principal.getId())) {
            throw new AccesoDenegadoException();
        }
    }

    private ForoPrincipal principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof ForoPrincipal principal)) {
            throw new AccesoDenegadoException();
        }
        return principal;
    }
}
