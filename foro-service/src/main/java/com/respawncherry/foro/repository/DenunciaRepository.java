package com.respawncherry.foro.repository;

import com.respawncherry.foro.entity.Denuncia;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    Optional<Denuncia> findByPublicacion_IdAndDenuncianteId(Long publicacionId, Long denuncianteId);

    List<Denuncia> findAllByOrderByFechaCreacionDesc();
}
