package com.respawncherry.foro.repository;

import com.respawncherry.foro.entity.PublicacionImagen;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicacionImagenRepository extends JpaRepository<PublicacionImagen, Long> {

    List<PublicacionImagen> findByPublicacion_IdOrderByOrdenAsc(Long publicacionId);

    Optional<PublicacionImagen> findByIdAndPublicacion_Id(Long id, Long publicacionId);

    long countByPublicacion_Id(Long publicacionId);

    @Query("select i.publicacion.id, i.id from PublicacionImagen i where i.publicacion.id in :ids order by i.orden asc, i.id asc")
    List<Object[]> idsPorPublicaciones(@Param("ids") Collection<Long> ids);
}
