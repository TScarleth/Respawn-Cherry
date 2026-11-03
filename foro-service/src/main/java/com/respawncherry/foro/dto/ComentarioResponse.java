package com.respawncherry.foro.dto;

import java.time.Instant;

public record ComentarioResponse(
        Long id,
        String contenido,
        Long publicacionId,
        Long autorId,
        Instant fechaCreacion) {
}
