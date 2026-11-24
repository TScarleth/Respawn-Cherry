package com.respawncherry.usuarios.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.respawncherry.usuarios.entity.Usuario;
import com.respawncherry.usuarios.exception.CorreoNoEnviadoException;
import com.respawncherry.usuarios.exception.CredencialesInvalidasException;
import com.respawncherry.usuarios.repository.UsuarioRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class VerificacionServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JdbcTemplate jdbc;

    private VerificacionService service;

    @BeforeEach
    void setUp() {
        service = new VerificacionService(
                mailSender,
                usuarioRepository,
                jdbc,
                "",
                "",
                "http://127.0.0.1:4200/");
    }

    @Test
    void enviarSinCredencialesNoGuardaToken() {
        assertThrows(CorreoNoEnviadoException.class, () -> service.enviar("cherry@respawn.com"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void confirmarRechazaTokenVacio() {
        assertThrows(CredencialesInvalidasException.class, () -> service.confirmar("  "));
        verifyNoInteractions(jdbc);
    }

    @Test
    void confirmarRechazaTokenDesconocido() {
        when(jdbc.queryForList(anyString(), eq("no-existe"))).thenReturn(List.of());
        assertThrows(CredencialesInvalidasException.class, () -> service.confirmar("no-existe"));
    }

    @Test
    void confirmarRechazaTokenCaducado() {
        when(jdbc.queryForList(anyString(), eq("caduco"))).thenReturn(List.of(Map.of(
                "CORREO", "cherry@respawn.com",
                "EXPIRA", Timestamp.from(Instant.now().minusSeconds(60)))));
        assertThrows(CredencialesInvalidasException.class, () -> service.confirmar("caduco"));
        verify(jdbc).update("DELETE FROM VERIFICACION_ENLACES WHERE TOKEN = ?", "caduco");
        verify(usuarioRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void confirmarMarcaLaCuentaVerificada() {
        when(jdbc.queryForList(anyString(), eq("valido"))).thenReturn(List.of(Map.of(
                "CORREO", "cherry@respawn.com",
                "EXPIRA", Timestamp.from(Instant.now().plusSeconds(3600)))));
        Usuario usuario = new Usuario();
        usuario.setVerificado(false);
        when(usuarioRepository.findByCorreo("cherry@respawn.com")).thenReturn(Optional.of(usuario));

        String correo = service.confirmar(" valido ");

        assertEquals("cherry@respawn.com", correo);
        assertTrue(usuario.isVerificado());
        verify(usuarioRepository).save(usuario);
        verify(jdbc).update("DELETE FROM VERIFICACION_ENLACES WHERE TOKEN = ?", "valido");
    }
}
