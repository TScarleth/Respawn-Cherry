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
import java.time.Instant;

@Entity
@Table(name = "COMENTARIOS")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comentario_seq")
    @SequenceGenerator(name = "comentario_seq", sequenceName = "COMENTARIO_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "publicacion_id", nullable = false)
    private Publicacion publicacion;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    protected Comentario() {
    }

    public Comentario(String contenido, Publicacion publicacion, Long autorId) {
        this.contenido = contenido;
        this.publicacion = publicacion;
        this.autorId = autorId;
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

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public Long getAutorId() {
        return autorId;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
