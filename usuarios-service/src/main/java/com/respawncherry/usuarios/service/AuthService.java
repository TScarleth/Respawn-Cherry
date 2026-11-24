package com.respawncherry.usuarios.service;

import com.respawncherry.usuarios.dto.LoginRequest;
import com.respawncherry.usuarios.dto.LoginResponse;
import com.respawncherry.usuarios.dto.RegistroRequest;
import com.respawncherry.usuarios.dto.UsuarioResponse;
import com.respawncherry.usuarios.entity.Usuario;
import com.respawncherry.usuarios.exception.CredencialesInvalidasException;
import com.respawncherry.usuarios.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificacionService verificacionService;

    public AuthService(
            UsuarioService usuarioService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            VerificacionService verificacionService) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.verificacionService = verificacionService;
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        UsuarioResponse creado = usuarioService.registrar(request);
        try {
            verificacionService.enviar(creado.getCorreo());
            return creado;
        } catch (RuntimeException ex) {
            usuarioService.eliminar(creado.getId());
            throw ex;
        }
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario;
        try {
            usuario = usuarioService.buscarPorCorreo(request.getCorreo());
        } catch (Exception ex) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        }
        if (!usuario.isVerificado()) {
            throw new CredencialesInvalidasException("Confirma tu correo antes de iniciar sesión");
        }

        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token, UsuarioMapper.toResponse(usuario));
    }
}
