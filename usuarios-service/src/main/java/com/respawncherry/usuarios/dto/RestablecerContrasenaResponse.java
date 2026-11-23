package com.respawncherry.usuarios.dto;

public class RestablecerContrasenaResponse {

    private String correo;

    public RestablecerContrasenaResponse() {
    }

    public RestablecerContrasenaResponse(String correo) {
        this.correo = correo;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}
