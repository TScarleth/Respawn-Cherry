package com.respawncherry.usuarios.dto;

import com.respawncherry.usuarios.validation.ContrasenaSegura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestablecerContrasenaRequest {

    @NotBlank
    private String token;

    @NotBlank
    @Size(min = 8, max = 72)
    @ContrasenaSegura
    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
