package com.respawncherry.foro.dto;

import com.respawncherry.foro.entity.EstadoDenuncia;
import jakarta.validation.constraints.NotNull;

public record DenunciaEstadoRequest(@NotNull EstadoDenuncia estado) {
}
