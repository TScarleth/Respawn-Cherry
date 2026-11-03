package com.respawncherry.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.respawncherry.usuarios.dto.UsuarioRequest;
import com.respawncherry.usuarios.dto.UsuarioResponse;
import com.respawncherry.usuarios.dto.UsuarioUpdateRequest;
import com.respawncherry.usuarios.entity.Rol;
import com.respawncherry.usuarios.exception.GlobalExceptionHandler;
import com.respawncherry.usuarios.service.UsuarioService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper.registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void listarUsuarios() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(
                new UsuarioResponse(1L, "Cherry", "cherry@respawn.com", Rol.USUARIO)));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("cherry@respawn.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[0].contrasena").doesNotExist());
    }

    @Test
    void crearUsuarioValidaCampos() throws Exception {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Cherry");
        request.setCorreo("correo-invalido");
        request.setPassword("123");
        request.setRol(Rol.USUARIO);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos inválidos"));
    }

    @Test
    void crearUsuarioDevuelve201() throws Exception {
        UsuarioRequest request = new UsuarioRequest();
        request.setNombre("Admin");
        request.setCorreo("admin@respawn.com");
        request.setPassword("admin1234");
        request.setRol(Rol.ADMIN);

        when(usuarioService.crear(any(UsuarioRequest.class)))
                .thenReturn(new UsuarioResponse(2L, "Admin", "admin@respawn.com", Rol.ADMIN));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void actualizarUsuario() throws Exception {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombre("Nuevo");

        when(usuarioService.actualizar(eq(1L), any(UsuarioUpdateRequest.class), any()))
                .thenReturn(new UsuarioResponse(1L, "Nuevo", "cherry@respawn.com", Rol.USUARIO));

        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo"));
    }

    @Test
    void eliminarUsuario() throws Exception {
        doNothing().when(usuarioService).eliminar(1L);

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());
    }
}
