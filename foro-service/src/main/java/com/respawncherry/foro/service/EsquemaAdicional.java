package com.respawncherry.foro.service;

import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EsquemaAdicional implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public EsquemaAdicional(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        secuencia("PUBLICACION_LIKE_SEQ");
        secuencia("DENUNCIA_SEQ");
        secuencia("IMAGEN_SEQ");
        if (!tabla("PUBLICACION_LIKES")) {
            jdbc.execute("""
                    CREATE TABLE PUBLICACION_LIKES (
                        ID NUMBER(19) PRIMARY KEY,
                        PUBLICACION_ID NUMBER(19) NOT NULL,
                        USUARIO_ID NUMBER(19) NOT NULL,
                        FECHA_CREACION TIMESTAMP WITH TIME ZONE NOT NULL,
                        CONSTRAINT UK_LIKE_PUBLICACION_USUARIO UNIQUE (PUBLICACION_ID, USUARIO_ID),
                        CONSTRAINT FK_LIKE_PUBLICACION FOREIGN KEY (PUBLICACION_ID) REFERENCES PUBLICACIONES(ID) ON DELETE CASCADE
                    )
                    """);
            jdbc.execute("CREATE INDEX IDX_LIKE_PUBLICACION ON PUBLICACION_LIKES(PUBLICACION_ID)");
        }
        if (!tabla("DENUNCIAS")) {
            jdbc.execute("""
                    CREATE TABLE DENUNCIAS (
                        ID NUMBER(19) PRIMARY KEY,
                        PUBLICACION_ID NUMBER(19) NOT NULL,
                        DENUNCIANTE_ID NUMBER(19) NOT NULL,
                        MOTIVO VARCHAR2(20 CHAR) NOT NULL,
                        EXPLICACION VARCHAR2(300 CHAR),
                        DETALLE VARCHAR2(500 CHAR),
                        ESTADO VARCHAR2(20 CHAR) NOT NULL,
                        FECHA_CREACION TIMESTAMP WITH TIME ZONE NOT NULL,
                        CONSTRAINT UK_DENUNCIA_PUBLICACION_USUARIO UNIQUE (PUBLICACION_ID, DENUNCIANTE_ID),
                        CONSTRAINT FK_DENUNCIA_PUBLICACION FOREIGN KEY (PUBLICACION_ID) REFERENCES PUBLICACIONES(ID) ON DELETE CASCADE
                    )
                    """);
            jdbc.execute("CREATE INDEX IDX_DENUNCIA_ESTADO ON DENUNCIAS(ESTADO)");
        }
        if (!tabla("PUBLICACION_IMAGENES")) {
            jdbc.execute("""
                    CREATE TABLE PUBLICACION_IMAGENES (
                        ID NUMBER(19) PRIMARY KEY,
                        PUBLICACION_ID NUMBER(19) NOT NULL,
                        CONTENIDO BLOB NOT NULL,
                        TIPO VARCHAR2(30 CHAR) NOT NULL,
                        ORDEN NUMBER(3) NOT NULL,
                        CONSTRAINT FK_IMAGEN_PUBLICACION FOREIGN KEY (PUBLICACION_ID) REFERENCES PUBLICACIONES(ID) ON DELETE CASCADE
                    )
                    """);
            jdbc.execute("CREATE INDEX IDX_IMAGEN_PUBLICACION ON PUBLICACION_IMAGENES(PUBLICACION_ID)");
        }
    }

    private void secuencia(String nombre) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME = ?", Integer.class, nombre);
        if (cantidad == null || cantidad == 0) {
            jdbc.execute("CREATE SEQUENCE " + nombre + " START WITH 1 INCREMENT BY 1");
        }
    }

    private boolean tabla(String nombre) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?", Integer.class, nombre);
        return cantidad != null && cantidad > 0;
    }

    static List<String> tablas() {
        return List.of("PUBLICACION_LIKES", "DENUNCIAS", "PUBLICACION_IMAGENES");
    }
}
