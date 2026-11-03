package com.respawncherry.foro.repository;

import com.respawncherry.foro.entity.Publicacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {

    @Query("""
            select p from Publicacion p
            where (:titulo is null or lower(p.titulo) like lower(concat('%', :titulo, '%')))
              and (:categoriaId is null or p.categoria.id = :categoriaId)
            order by p.fechaCreacion desc
            """)
    List<Publicacion> buscar(@Param("titulo") String titulo, @Param("categoriaId") Long categoriaId);
}
