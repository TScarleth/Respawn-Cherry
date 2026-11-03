package com.respawncherry.usuarios.service;

import com.respawncherry.usuarios.dto.UsuarioResponse;
import com.respawncherry.usuarios.entity.Usuario;

public final class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol());
    }
}
