package com.respawncherry.foro.controller;

import com.respawncherry.foro.dto.ComentarioRequest;
import com.respawncherry.foro.dto.ComentarioResponse;
import com.respawncherry.foro.dto.PublicacionRequest;
import com.respawncherry.foro.dto.PublicacionResponse;
import com.respawncherry.foro.service.ComentarioService;
import com.respawncherry.foro.service.PublicacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final ComentarioService comentarioService;

    public PublicacionController(PublicacionService publicacionService, ComentarioService comentarioService) {
        this.publicacionService = publicacionService;
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ResponseEntity<List<PublicacionResponse>> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(publicacionService.listar(titulo, categoriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(publicacionService.obtener(id));
    }

    @PostMapping
    public ResponseEntity<PublicacionResponse> crear(
            @Valid @RequestBody PublicacionRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionService.crear(request, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PublicacionRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(publicacionService.actualizar(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        publicacionService.eliminar(id, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioResponse>> listarComentarios(@PathVariable Long id) {
        return ResponseEntity.ok(comentarioService.listarPorPublicacion(id));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> crearComentario(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comentarioService.crear(id, request, authentication));
    }
}
