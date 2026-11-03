package com.respawncherry.foro.repository;

import com.respawncherry.foro.entity.Comentario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByPublicacion_IdOrderByFechaCreacionAsc(Long publicacionId);
}
