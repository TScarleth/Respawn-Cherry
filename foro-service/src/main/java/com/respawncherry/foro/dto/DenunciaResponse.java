package com.respawncherry.foro.dto;

import com.respawncherry.foro.entity.EstadoDenuncia;
import com.respawncherry.foro.entity.MotivoDenuncia;
import java.time.Instant;

public record DenunciaResponse(
        Long id,
        Long publicacionId,
        String titulo,
        Long autoraId,
        Long denuncianteId,
        MotivoDenuncia motivo,
        String explicacion,
        String detalle,
        EstadoDenuncia estado,
        Instant fechaCreacion) {
}
