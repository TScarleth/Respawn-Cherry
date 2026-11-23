package com.respawncherry.usuarios.dto;

import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.validation.ContrasenaSegura;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateRequest {

    @Size(max = 100)
    private String nombre;

    @Email
    @Size(max = 150)
    private String correo;

    @Size(min = 8, max = 72)
    @ContrasenaSegura
    private String password;

    private Rol rol;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
