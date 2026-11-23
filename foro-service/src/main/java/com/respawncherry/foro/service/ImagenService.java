package com.respawncherry.foro.service;

import com.respawncherry.foro.entity.Publicacion;
import com.respawncherry.foro.entity.PublicacionImagen;
import com.respawncherry.foro.exception.AccesoDenegadoException;
import com.respawncherry.foro.exception.RecursoNoEncontradoException;
import com.respawncherry.foro.exception.SolicitudInvalidaException;
import com.respawncherry.foro.repository.PublicacionImagenRepository;
import com.respawncherry.foro.repository.PublicacionRepository;
import com.respawncherry.foro.security.ForoPrincipal;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagenService {

    static final int MAXIMO = 4;
    static final int TAMANO_MAXIMO = 2 * 1024 * 1024;

    private final PublicacionRepository publicacionRepository;
    private final PublicacionImagenRepository imagenRepository;

    public ImagenService(PublicacionRepository publicacionRepository, PublicacionImagenRepository imagenRepository) {
        this.publicacionRepository = publicacionRepository;
        this.imagenRepository = imagenRepository;
    }

    @Transactional
    public List<Long> guardar(Long publicacionId, List<MultipartFile> archivos, Authentication authentication) {
        if (archivos == null || archivos.isEmpty()) {
            throw new SolicitudInvalidaException("Selecciona al menos una imagen");
        }
        Publicacion publicacion = publicacionRepository.findById(publicacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Publicación no encontrada"));
        ForoPrincipal usuaria = principal(authentication);
        if (!usuaria.esAdmin() && !publicacion.getAutorId().equals(usuaria.getId())) {
            throw new AccesoDenegadoException();
        }
        long actuales = imagenRepository.countByPublicacion_Id(publicacionId);
        if (actuales + archivos.size() > MAXIMO) {
            throw new SolicitudInvalidaException("Una publicación puede tener hasta 4 imágenes");
        }
        List<Long> ids = new ArrayList<>();
        int orden = (int) actuales;
        for (MultipartFile archivo : archivos) {
            byte[] bytes = leer(archivo);
            String tipo = tipoSeguro(bytes);
            PublicacionImagen imagen = imagenRepository.save(new PublicacionImagen(publicacion, bytes, tipo, orden));
            ids.add(imagen.getId());
            orden++;
        }
        return ids;
    }

    @Transactional(readOnly = true)
    public PublicacionImagen obtener(Long publicacionId, Long imagenId) {
        return imagenRepository.findByIdAndPublicacion_Id(imagenId, publicacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Imagen no encontrada"));
    }

    private byte[] leer(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("El archivo está vacío");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new SolicitudInvalidaException("Cada imagen puede pesar hasta 2 MB");
        }
        try {
            return archivo.getBytes();
        } catch (IOException ex) {
            throw new SolicitudInvalidaException("No se pudo leer la imagen");
        }
    }

    static String tipoSeguro(byte[] bytes) {
        if (esJpeg(bytes)) return "image/jpeg";
        if (esPng(bytes)) return "image/png";
        if (esWebp(bytes)) return "image/webp";
        throw new SolicitudInvalidaException("Solo se aceptan imágenes JPG, PNG o WebP");
    }

    private static boolean esJpeg(byte[] bytes) {
        return bytes.length > 3 && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
    }

    private static boolean esPng(byte[] bytes) {
        return bytes.length > 8
                && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47;
    }

    private static boolean esWebp(byte[] bytes) {
        return bytes.length > 12
                && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private ForoPrincipal principal(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof ForoPrincipal principal)) {
            throw new AccesoDenegadoException();
        }
        return principal;
    }
}
