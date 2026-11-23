package com.respawncherry.usuarios.controller;

import com.respawncherry.usuarios.dto.LoginRequest;
import com.respawncherry.usuarios.dto.LoginResponse;
import com.respawncherry.usuarios.dto.RecuperarCorreoRequest;
import com.respawncherry.usuarios.dto.RegistroRequest;
import com.respawncherry.usuarios.dto.RestablecerContrasenaRequest;
import com.respawncherry.usuarios.dto.RestablecerContrasenaResponse;
import com.respawncherry.usuarios.dto.UsuarioResponse;
import com.respawncherry.usuarios.service.AuthService;
import com.respawncherry.usuarios.service.PasswordResetService;
import com.respawncherry.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService, UsuarioService usuarioService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recuperar")
    public ResponseEntity<Void> recuperar(@Valid @RequestBody RecuperarCorreoRequest request) {
        passwordResetService.enviarEnlace(request.getCorreo());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/restablecer")
    public ResponseEntity<RestablecerContrasenaResponse> restablecer(@Valid @RequestBody RestablecerContrasenaRequest request) {
        String correo = passwordResetService.consumirToken(request.getToken());
        usuarioService.cambiarPassword(correo, request.getPassword());
        return ResponseEntity.ok(new RestablecerContrasenaResponse(correo));
    }
}
