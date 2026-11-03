package com.respawncherry.usuarios.dto;

import java.time.Instant;
import java.util.List;

public class ErrorResponse {

    private Instant timestamp = Instant.now();
    private int status;
    private String mensaje;
    private List<String> detalles;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String mensaje, List<String> detalles) {
        this.status = status;
        this.mensaje = mensaje;
        this.detalles = detalles;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<String> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<String> detalles) {
        this.detalles = detalles;
    }
}
