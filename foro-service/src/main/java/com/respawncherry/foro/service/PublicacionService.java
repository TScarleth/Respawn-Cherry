package com.respawncherry.foro.service;

import com.respawncherry.foro.dto.PublicacionRequest;
import com.respawncherry.foro.dto.PublicacionResponse;
import com.respawncherry.foro.entity.Categoria;
import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.exception.AccesoDenegadoException;
import com.respawncherry.foro.exception.RecursoNoEncontradoException;
import com.respawncherry.foro.repository.CategoriaRepository;
import com.respawncherry.foro.repository.PublicacionImagenRepository;
import com.respawncherry.foro.repository.PublicacionLikeRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final CategoriaRepository categoriaRepository;
    private final PublicacionLikeRepository likeRepository;
    private final PublicacionImagenRepository imagenRepository;

    public PublicacionService(
            PublicacionRepository publicacionRepository,
            CategoriaRepository categoriaRepository,
            PublicacionLikeRepository likeRepository,
            PublicacionImagenRepository imagenRepository) {
        this.publicacionRepository = publicacionRepository;
        this.categoriaRepository = categoriaRepository;
        this.likeRepository = likeRepository;
        this.imagenRepository = imagenRepository;
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponse> listar(String titulo, Long categoriaId, Authentication authentication) {
        String filtro = titulo == null || titulo.isBlank() ? null : titulo.trim();
        return responder(publicacionRepository.buscar(filtro, categoriaId), authentication);
    }

    @Transactional(readOnly = true)
    public PublicacionResponse obtener(Long id, Authentication authentication) {
        return responder(List.of(buscar(id)), authentication).get(0);
    }

    @Transactional
    public PublicacionResponse crear(PublicacionRequest request, Authentication authentication) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
        ForoPrincipal autor = principal(authentication);
        return responder(List.of(publicacionRepository.save(
                new Publicacion(request.titulo().trim(), request.contenido().trim(), categoria, autor.getId()))), authentication).get(0);
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
        return responder(List.of(publicacionRepository.save(publicacion)), authentication).get(0);
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

    private List<PublicacionResponse> responder(List<Publicacion> publicaciones, Authentication authentication) {
        if (publicaciones.isEmpty()) {
            return List.of();
        }
        List<Long> ids = publicaciones.stream().map(Publicacion::getId).toList();
        Map<Long, Long> conteo = new HashMap<>();
        for (Object[] fila : likeRepository.contarPorPublicaciones(ids)) {
            conteo.put(numero(fila[0]), numero(fila[1]));
        }
        Set<Long> mias = new HashSet<>();
        Long usuarioId = usuarioActual(authentication);
        if (usuarioId != null) {
            for (Number id : likeRepository.gustadasPor(usuarioId, ids)) {
                mias.add(id.longValue());
            }
        }
        Map<Long, List<Long>> imagenes = new HashMap<>();
        for (Object[] fila : imagenRepository.idsPorPublicaciones(ids)) {
            imagenes.computeIfAbsent(numero(fila[0]), ignored -> new ArrayList<>()).add(numero(fila[1]));
        }
        return publicaciones.stream()
                .map(p -> new PublicacionResponse(
                        p.getId(), p.getTitulo(), p.getContenido(), p.getCategoria().getId(),
                        p.getCategoria().getNombre(), p.getAutorId(), p.getFechaCreacion(),
                        conteo.getOrDefault(p.getId(), 0L), mias.contains(p.getId()),
                        imagenes.getOrDefault(p.getId(), List.of())))
                .toList();
    }

    private static long numero(Object valor) {
        if (valor instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(valor));
    }

    private Long usuarioActual(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof ForoPrincipal principal) {
            return principal.getId();
        }
        return null;
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
