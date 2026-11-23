package com.respawncherry.foro.exception;

public class AccesoDenegadoException extends RuntimeException {
    public AccesoDenegadoException() {
        super("Solo el autor o un administrador puede modificar este contenido");
    }

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
