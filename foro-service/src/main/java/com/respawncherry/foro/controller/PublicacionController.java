package com.respawncherry.foro.controller;

import com.respawncherry.foro.dto.ComentarioRequest;
import com.respawncherry.foro.dto.ComentarioResponse;
import com.respawncherry.foro.dto.LikeResponse;
import com.respawncherry.foro.dto.PublicacionRequest;
import com.respawncherry.foro.dto.PublicacionResponse;
import com.respawncherry.foro.entity.PublicacionImagen;
import com.respawncherry.foro.service.ComentarioService;
import com.respawncherry.foro.service.ImagenService;
import com.respawncherry.foro.service.LikeService;
import com.respawncherry.foro.service.PublicacionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final ComentarioService comentarioService;
    private final LikeService likeService;
    private final ImagenService imagenService;

    public PublicacionController(
            PublicacionService publicacionService,
            ComentarioService comentarioService,
            LikeService likeService,
            ImagenService imagenService) {
        this.publicacionService = publicacionService;
        this.comentarioService = comentarioService;
        this.likeService = likeService;
        this.imagenService = imagenService;
    }

    @GetMapping
    public ResponseEntity<List<PublicacionResponse>> listar(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) Long categoriaId,
            Authentication authentication) {
        return ResponseEntity.ok(publicacionService.listar(titulo, categoriaId, authentication));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionResponse> obtener(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(publicacionService.obtener(id, authentication));
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

    @PostMapping("/{id}/likes")
    public ResponseEntity<LikeResponse> alternarLike(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(likeService.alternar(id, authentication));
    }

    @PostMapping(value = "/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<Long>> subirImagenes(
            @PathVariable Long id,
            @RequestParam("archivos") List<MultipartFile> archivos,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imagenService.guardar(id, archivos, authentication));
    }

    @GetMapping("/{id}/imagenes/{imagenId}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable Long id, @PathVariable Long imagenId) {
        PublicacionImagen imagen = imagenService.obtener(id, imagenId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=86400")
                .contentType(MediaType.parseMediaType(imagen.getTipo()))
                .body(imagen.getContenido());
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
