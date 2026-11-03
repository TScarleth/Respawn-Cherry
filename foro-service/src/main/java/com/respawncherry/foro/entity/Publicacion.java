package com.respawncherry.foro.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PUBLICACIONES")
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "publicacion_seq")
    @SequenceGenerator(name = "publicacion_seq", sequenceName = "PUBLICACION_SEQ", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 4000)
    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comentario> comentarios = new ArrayList<>();

    protected Publicacion() {
    }

    public Publicacion(String titulo, String contenido, Categoria categoria, Long autorId) {
        this.titulo = titulo;
        this.contenido = contenido;
        this.categoria = categoria;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Long getAutorId() {
        return autorId;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
