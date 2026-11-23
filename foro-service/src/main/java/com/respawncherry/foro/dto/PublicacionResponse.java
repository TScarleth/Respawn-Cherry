package com.respawncherry.foro.dto;

import java.time.Instant;
import java.util.List;

public record PublicacionResponse(
        Long id,
        String titulo,
        String contenido,
        Long categoriaId,
        String categoria,
        Long autorId,
        Instant fechaCreacion,
        long likes,
        boolean meGusta,
        List<Long> imagenIds) {
}
