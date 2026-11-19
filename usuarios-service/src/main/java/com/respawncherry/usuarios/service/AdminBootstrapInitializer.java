package com.respawncherry.usuarios.service;

import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.entity.Usuario;
import com.respawncherry.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "respawncherry.bootstrap.admin.enabled", havingValue = "true")
public class AdminBootstrapInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String nombre;
    private final String correo;
    private final String password;

    public AdminBootstrapInitializer(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${respawncherry.bootstrap.admin.name}") String nombre,
            @Value("${respawncherry.bootstrap.admin.correo}") String correo,
            @Value("${respawncherry.bootstrap.admin.password}") String password) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.nombre = nombre;
        this.correo = correo;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (password.isBlank()) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD es obligatorio cuando el bootstrap está habilitado");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre(nombre);
        admin.setCorreo(correo);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRol(Rol.ADMIN);
        usuarioRepository.save(admin);
    }
}
