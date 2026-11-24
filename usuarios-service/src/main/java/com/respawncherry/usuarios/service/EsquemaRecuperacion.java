package com.respawncherry.usuarios.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EsquemaRecuperacion implements ApplicationRunner {

    private final JdbcTemplate jdbc;

    public EsquemaRecuperacion(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!tabla("RECUPERACION_ENLACES")) {
            jdbc.execute("""
                    CREATE TABLE RECUPERACION_ENLACES (
                        TOKEN VARCHAR2(36 CHAR) PRIMARY KEY,
                        CORREO VARCHAR2(150 CHAR) NOT NULL,
                        EXPIRA TIMESTAMP WITH TIME ZONE NOT NULL
                    )
                    """);
        }
        if (!tabla("VERIFICACION_ENLACES")) {
            jdbc.execute("""
                    CREATE TABLE VERIFICACION_ENLACES (
                        TOKEN VARCHAR2(36 CHAR) PRIMARY KEY,
                        CORREO VARCHAR2(150 CHAR) NOT NULL,
                        EXPIRA TIMESTAMP WITH TIME ZONE NOT NULL
                    )
                    """);
        }
        if (!columna("USUARIOS", "VERIFICADO")) {
            jdbc.execute("ALTER TABLE USUARIOS ADD VERIFICADO NUMBER(1) DEFAULT 1 NOT NULL");
        }
    }

    private boolean tabla(String nombre) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = ?",
                Integer.class,
                nombre);
        return cantidad != null && cantidad > 0;
    }

    private boolean columna(String tabla, String nombre) {
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USER_TAB_COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tabla,
                nombre);
        return cantidad != null && cantidad > 0;
    }
}
