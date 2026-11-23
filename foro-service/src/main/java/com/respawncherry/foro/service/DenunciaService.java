package com.respawncherry.foro.service;

import com.respawncherry.foro.dto.DenunciaEstadoRequest;
import com.respawncherry.foro.dto.DenunciaRecibo;
import com.respawncherry.foro.dto.DenunciaRequest;
import com.respawncherry.foro.dto.DenunciaResponse;
import com.respawncherry.foro.entity.Denuncia;
import com.respawncherry.foro.entity.EstadoDenuncia;
import com.respawncherry.foro.entity.MotivoDenuncia;
import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.exception.AccesoDenegadoException;
import com.respawncherry.foro.exception.ConflictoException;
import com.respawncherry.foro.exception.RecursoNoEncontradoException;
import com.respawncherry.foro.exception.SolicitudInvalidaException;
import com.respawncherry.foro.repository.DenunciaRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DenunciaService {

    private final DenunciaRepository denunciaRepository;
    private final PublicacionRepository publicacionRepository;

    public DenunciaService(DenunciaRepository denunciaRepository, PublicacionRepository publicacionRepository) {
        this.denunciaRepository = denunciaRepository;
        this.publicacionRepository = publicacionRepository;
    }

    @Transactional
    public DenunciaRecibo crear(DenunciaRequest request, Authentication authentication) {
        ForoPrincipal usuaria = principal(authentication);
        Publicacion publicacion = publicacionRepository.findById(request.publicacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Publicación no encontrada"));
        if (denunciaRepository.findByPublicacion_IdAndDenuncianteId(publicacion.getId(), usuaria.getId()).isPresent()) {
            throw new ConflictoException("Ya enviaste una denuncia sobre esta publicación");
        }
        String explicacion = limpio(request.explicacion());
        String detalle = limpio(request.detalle());
        if (request.motivo() == MotivoDenuncia.OTRO && explicacion == null) {
            throw new SolicitudInvalidaException("Cuéntanos el motivo cuando eliges Otro");
        }
        Denuncia denuncia = denunciaRepository.save(
                new Denuncia(publicacion, usuaria.getId(), request.motivo(), explicacion, detalle));
        return new DenunciaRecibo(denuncia.getId(), denuncia.getEstado());
    }

    @Transactional(readOnly = true)
    public List<DenunciaResponse> listar(Authentication authentication) {
        exigirAdmin(authentication);
        return denunciaRepository.findAllByOrderByFechaCreacionDesc().stream().map(this::respuesta).toList();
    }

    @Transactional
    public DenunciaResponse actualizarEstado(Long id, DenunciaEstadoRequest request, Authentication authentication) {
        exigirAdmin(authentication);
        if (request.estado() == EstadoDenuncia.PENDIENTE) {
            throw new SolicitudInvalidaException("El estado solo puede pasar a revisada o resuelta");
        }
        Denuncia denuncia = denunciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Denuncia no encontrada"));
        denuncia.setEstado(request.estado());
        return respuesta(denunciaRepository.save(denuncia));
    }

    private DenunciaResponse respuesta(Denuncia denuncia) {
        Publicacion publicacion = denuncia.getPublicacion();
        return new DenunciaResponse(
                denuncia.getId(),
                publicacion.getId(),
                publicacion.getTitulo(),
                publicacion.getAutorId(),
                denuncia.getDenuncianteId(),
                denuncia.getMotivo(),
                denuncia.getExplicacion(),
                denuncia.getDetalle(),
                denuncia.getEstado(),
                denuncia.getFechaCreacion());
    }

    private String limpio(String valor) {
        if (valor == null) return null;
        String texto = valor.trim();
        return texto.isEmpty() ? null : texto;
    }

    private void exigirAdmin(Authentication authentication) {
        if (!principal(authentication).esAdmin()) {
            throw new AccesoDenegadoException("Solo una cuenta administradora puede ver las denuncias");
        }
    }

    private ForoPrincipal principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof ForoPrincipal principal)) {
            throw new AccesoDenegadoException();
        }
        return principal;
    }
}
