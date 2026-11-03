package com.respawncherry.foro.dto;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(Instant timestamp, int status, String mensaje, List<String> detalles) {
    public ErrorResponse(int status, String mensaje, List<String> detalles) {
        this(Instant.now(), status, mensaje, detalles);
    }
}
