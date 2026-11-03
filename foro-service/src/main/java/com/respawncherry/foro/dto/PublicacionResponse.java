package com.respawncherry.foro.dto;

import java.time.Instant;

public record PublicacionResponse(
        Long id,
        String titulo,
        String contenido,
        Long categoriaId,
        String categoria,
        Long autorId,
        Instant fechaCreacion) {
}
