package com.respawncherry.foro.repository;

import com.respawncherry.foro.entity.PublicacionLike;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicacionLikeRepository extends JpaRepository<PublicacionLike, Long> {

    Optional<PublicacionLike> findByPublicacion_IdAndUsuarioId(Long publicacionId, Long usuarioId);

    long countByPublicacion_Id(Long publicacionId);

    @Query("select l.publicacion.id, count(l) from PublicacionLike l where l.publicacion.id in :ids group by l.publicacion.id")
    List<Object[]> contarPorPublicaciones(@Param("ids") Collection<Long> ids);

    @Query("select l.publicacion.id from PublicacionLike l where l.usuarioId = :usuarioId and l.publicacion.id in :ids")
    List<Number> gustadasPor(@Param("usuarioId") Long usuarioId, @Param("ids") Collection<Long> ids);
}
