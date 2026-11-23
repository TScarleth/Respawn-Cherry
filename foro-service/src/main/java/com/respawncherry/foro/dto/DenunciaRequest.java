package com.respawncherry.foro.dto;

import com.respawncherry.foro.entity.MotivoDenuncia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DenunciaRequest(
        @NotNull Long publicacionId,
        @NotNull MotivoDenuncia motivo,
        @Size(max = 300) String explicacion,
        @Size(max = 500) String detalle) {
}
