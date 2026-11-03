package com.respawncherry.foro.service;

import com.respawncherry.foro.dto.PublicacionRequest;
import com.respawncherry.foro.dto.PublicacionResponse;
import com.respawncherry.foro.entity.Categoria;
import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.exception.AccesoDenegadoException;
import com.respawncherry.foro.exception.RecursoNoEncontradoException;
import com.respawncherry.foro.repository.CategoriaRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final CategoriaRepository categoriaRepository;

    public PublicacionService(
            PublicacionRepository publicacionRepository, CategoriaRepository categoriaRepository) {
        this.publicacionRepository = publicacionRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> listar(String titulo, Long categoriaId) {
        String filtro = titulo == null || titulo.isBlank() ? null : titulo.trim();
        return publicacionRepository.buscar(filtro, categoriaId).stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public PublicacionResponse obtener(Long id) {
        return respuesta(buscar(id));
    }

    @Transactional
    public PublicacionResponse crear(PublicacionRequest request, Authentication authentication) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
        ForoPrincipal autor = principal(authentication);
        return respuesta(publicacionRepository.save(
                new Publicacion(request.titulo().trim(), request.contenido().trim(), categoria, autor.getId())));
    }

    @Transactional
    public PublicacionResponse actualizar(Long id, PublicacionRequest request, Authentication authentication) {
        Publicacion publicacion = buscar(id);
        autorizar(publicacion.getAutorId(), authentication);
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
        publicacion.setTitulo(request.titulo().trim());
        publicacion.setContenido(request.contenido().trim());
        publicacion.setCategoria(categoria);
        return respuesta(publicacionRepository.save(publicacion));
    }

    @Transactional
    public void eliminar(Long id, Authentication authentication) {
        Publicacion publicacion = buscar(id);
        autorizar(publicacion.getAutorId(), authentication);
        publicacionRepository.delete(publicacion);
    }

    private Publicacion buscar(Long id) {
        return publicacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Publicación no encontrada"));
    }

    private PublicacionResponse respuesta(Publicacion p) {
        return new PublicacionResponse(
                p.getId(), p.getTitulo(), p.getContenido(), p.getCategoria().getId(),
                p.getCategoria().getNombre(), p.getAutorId(), p.getFechaCreacion());
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
