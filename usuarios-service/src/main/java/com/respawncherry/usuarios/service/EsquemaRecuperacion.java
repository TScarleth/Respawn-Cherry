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
        Integer cantidad = jdbc.queryForObject(
                "SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME = 'RECUPERACION_ENLACES'",
                Integer.class);
        if (cantidad != null && cantidad > 0) {
            return;
        }
        jdbc.execute("""
                CREATE TABLE RECUPERACION_ENLACES (
                    TOKEN VARCHAR2(36 CHAR) PRIMARY KEY,
                    CORREO VARCHAR2(150 CHAR) NOT NULL,
                    EXPIRA TIMESTAMP WITH TIME ZONE NOT NULL
                )
                """);
    }
}
