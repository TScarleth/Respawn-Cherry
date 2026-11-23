package com.respawncherry.usuarios.service;

import com.respawncherry.usuarios.entity.Usuario;
import com.respawncherry.usuarios.exception.CorreoNoEnviadoException;
import com.respawncherry.usuarios.exception.CredencialesInvalidasException;
import com.respawncherry.usuarios.exception.RecursoNoEncontradoException;
import com.respawncherry.usuarios.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

    private static final long VIGENCIA_MS = 30 * 60 * 1000L;

    private final JavaMailSender mailSender;
    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbc;
    private final String username;
    private final String from;
    private final String frontendUrl;

    public PasswordResetService(
            JavaMailSender mailSender,
            UsuarioRepository usuarioRepository,
            JdbcTemplate jdbc,
            @Value("${spring.mail.username:}") String username,
            @Value("${respawncherry.mail.from:}") String from,
            @Value("${respawncherry.frontend-url:http://127.0.0.1:4200}") String frontendUrl) {
        this.mailSender = mailSender;
        this.usuarioRepository = usuarioRepository;
        this.jdbc = jdbc;
        this.username = username;
        this.from = from == null || from.isBlank() ? username : from;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
    }

    public void enviarEnlace(String correo) {
        if (username == null || username.isBlank()) {
            throw new CorreoNoEnviadoException("El correo de Respawn Cherry todavía no está configurado");
        }
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo.trim())
                .orElseThrow(() -> new RecursoNoEncontradoException("No encontramos una cuenta con ese correo"));
        String token = UUID.randomUUID().toString();
        Instant expira = Instant.now().plusMillis(VIGENCIA_MS);
        jdbc.update(
                "INSERT INTO RECUPERACION_ENLACES (TOKEN, CORREO, EXPIRA) VALUES (?, ?, ?)",
                token,
                usuario.getCorreo(),
                Timestamp.from(expira));
        String enlace = frontendUrl + "/restablecer?token=" + token;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(usuario.getCorreo());
            helper.setSubject("Recupera tu contraseña — Respawn Cherry");
            helper.setText("""
                    Hola,

                    ¿Has olvidado tu contraseña? Recupérala con este enlace:
                    %s

                    El enlace caduca en 30 minutos. Si no pediste este cambio, puedes ignorar el mensaje.

                    Respawn Cherry
                    """.formatted(enlace));
            mailSender.send(message);
        } catch (MessagingException | RuntimeException ex) {
            jdbc.update("DELETE FROM RECUPERACION_ENLACES WHERE TOKEN = ?", token);
            throw new CorreoNoEnviadoException("No pudimos enviar el correo. Inténtalo de nuevo.");
        }
    }

    public String consumirToken(String token) {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT CORREO, EXPIRA FROM RECUPERACION_ENLACES WHERE TOKEN = ?",
                token);
        if (filas.isEmpty()) {
            throw new CredencialesInvalidasException("El enlace no es válido o ya caducó");
        }
        Map<String, Object> fila = filas.get(0);
        jdbc.update("DELETE FROM RECUPERACION_ENLACES WHERE TOKEN = ?", token);
        Object expira = fila.get("EXPIRA");
        if (expira == null) {
            expira = fila.get("expira");
        }
        if (aInstant(expira).isBefore(Instant.now())) {
            throw new CredencialesInvalidasException("El enlace no es válido o ya caducó");
        }
        Object correo = fila.get("CORREO");
        if (correo == null) {
            correo = fila.get("correo");
        }
        return String.valueOf(correo);
    }

    private static Instant aInstant(Object valor) {
        if (valor instanceof Timestamp marca) {
            return marca.toInstant();
        }
        if (valor instanceof OffsetDateTime fecha) {
            return fecha.toInstant();
        }
        if (valor instanceof Instant instant) {
            return instant;
        }
        throw new CredencialesInvalidasException("El enlace no es válido o ya caducó");
    }
}
