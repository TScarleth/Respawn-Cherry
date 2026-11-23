package com.respawncherry.foro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "PUBLICACION_LIKES", uniqueConstraints = @UniqueConstraint(columnNames = {"PUBLICACION_ID", "USUARIO_ID"}))
public class PublicacionLike {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "like_seq")
    @SequenceGenerator(name = "like_seq", sequenceName = "PUBLICACION_LIKE_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PUBLICACION_ID", nullable = false)
    private Publicacion publicacion;

    @Column(name = "USUARIO_ID", nullable = false)
    private Long usuarioId;

    @Column(name = "FECHA_CREACION", nullable = false)
    private Instant fechaCreacion;

    protected PublicacionLike() {
    }

    public PublicacionLike(Publicacion publicacion, Long usuarioId) {
        this.publicacion = publicacion;
        this.usuarioId = usuarioId;
    }

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
}
