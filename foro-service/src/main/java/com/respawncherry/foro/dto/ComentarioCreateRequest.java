package com.respawncherry.foro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComentarioCreateRequest(
        @NotNull Long publicacionId,
        @NotBlank @Size(max = 2000) String contenido) {
}
