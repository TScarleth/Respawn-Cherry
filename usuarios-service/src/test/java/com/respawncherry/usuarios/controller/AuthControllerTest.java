package com.respawncherry.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.respawncherry.usuarios.dto.LoginRequest;
import com.respawncherry.usuarios.dto.LoginResponse;
import com.respawncherry.usuarios.dto.RegistroRequest;
import com.respawncherry.usuarios.dto.UsuarioResponse;
import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.exception.GlobalExceptionHandler;
import com.respawncherry.usuarios.service.AuthService;
import com.respawncherry.usuarios.service.PasswordResetService;
import com.respawncherry.usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registroDevuelve201() throws Exception {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Cherry");
        request.setCorreo("cherry@respawn.com");
        request.setPassword("secreto123");

        when(authService.registrar(any(RegistroRequest.class)))
                .thenReturn(new UsuarioResponse(1L, "Cherry", "cherry@respawn.com", Rol.USUARIO));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correo").value("cherry@respawn.com"))
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void loginDevuelveToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCorreo("cherry@respawn.com");
        request.setPassword("secreto123");

        LoginResponse respuesta = new LoginResponse(
                "token.jwt",
                new UsuarioResponse(1L, "Cherry", "cherry@respawn.com", Rol.USUARIO));
        when(authService.login(any(LoginRequest.class))).thenReturn(respuesta);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.jwt"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }
}
