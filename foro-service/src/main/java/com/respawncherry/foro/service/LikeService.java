package com.respawncherry.foro.service;

import com.respawncherry.foro.dto.LikeResponse;
import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.entity.PublicacionLike;
import com.respawncherry.foro.exception.AccesoDenegadoException;
import com.respawncherry.foro.exception.RecursoNoEncontradoException;
import com.respawncherry.foro.repository.PublicacionLikeRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

    private final PublicacionRepository publicacionRepository;
    private final PublicacionLikeRepository likeRepository;

    public LikeService(PublicacionRepository publicacionRepository, PublicacionLikeRepository likeRepository) {
        this.publicacionRepository = publicacionRepository;
        this.likeRepository = likeRepository;
    }

    @Transactional
    public LikeResponse alternar(Long publicacionId, Authentication authentication) {
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Publicación no encontrada"));
        ForoPrincipal usuaria = principal(authentication);
        Optional<PublicacionLike> existente = likeRepository.findByPublicacion_IdAndUsuarioId(publicacionId, usuaria.getId());
        if (existente.isPresent()) {
            likeRepository.delete(existente.get());
            likeRepository.flush();
        } else {
            likeRepository.save(new PublicacionLike(publicacion, usuaria.getId()));
        }
        boolean meGusta = likeRepository.findByPublicacion_IdAndUsuarioId(publicacionId, usuaria.getId()).isPresent();
        return new LikeResponse(likeRepository.countByPublicacion_Id(publicacionId), meGusta);
    }

    private ForoPrincipal principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof ForoPrincipal principal)) {
            throw new AccesoDenegadoException();
        }
        return principal;
    }
}
