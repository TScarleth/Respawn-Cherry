package com.respawncherry.foro.controller;

import com.respawncherry.foro.dto.ComentarioRequest;
import com.respawncherry.foro.dto.ComentarioCreateRequest;
import com.respawncherry.foro.dto.ComentarioResponse;
import com.respawncherry.foro.service.ComentarioService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponse>> listar() {
        return ResponseEntity.ok(comentarioService.listar());
    }

    @PostMapping
    public ResponseEntity<ComentarioResponse> crear(
            @Valid @RequestBody ComentarioCreateRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.crear(
                request.publicacionId(), new ComentarioRequest(request.contenido()), authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(comentarioService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComentarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(comentarioService.actualizar(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication authentication) {
        comentarioService.eliminar(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
