package com.respawncherry.foro.controller;

import com.respawncherry.foro.dto.DenunciaEstadoRequest;
import com.respawncherry.foro.dto.DenunciaRecibo;
import com.respawncherry.foro.dto.DenunciaRequest;
import com.respawncherry.foro.dto.DenunciaResponse;
import com.respawncherry.foro.service.DenunciaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/denuncias")
public class DenunciaController {

    private final DenunciaService denunciaService;

    public DenunciaController(DenunciaService denunciaService) {
        this.denunciaService = denunciaService;
    }

    @PostMapping
    public ResponseEntity<DenunciaRecibo> crear(
            @Valid @RequestBody DenunciaRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(denunciaService.crear(request, authentication));
    }

    @GetMapping
    public ResponseEntity<List<DenunciaResponse>> listar(Authentication authentication) {
        return ResponseEntity.ok(denunciaService.listar(authentication));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<DenunciaResponse> estado(
            @PathVariable Long id,
            @Valid @RequestBody DenunciaEstadoRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(denunciaService.actualizarEstado(id, request, authentication));
    }
}
