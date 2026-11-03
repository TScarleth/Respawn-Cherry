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

    public AuthService(UsuarioService usuarioService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        return usuarioService.registrar(request);
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

        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token, UsuarioMapper.toResponse(usuario));
    }
}
