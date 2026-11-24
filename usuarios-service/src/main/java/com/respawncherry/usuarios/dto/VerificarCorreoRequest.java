package com.respawncherry.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public class VerificarCorreoRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
