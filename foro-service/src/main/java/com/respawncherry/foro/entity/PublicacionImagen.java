package com.respawncherry.foro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "PUBLICACION_IMAGENES")
public class PublicacionImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imagen_seq")
    @SequenceGenerator(name = "imagen_seq", sequenceName = "IMAGEN_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PUBLICACION_ID", nullable = false)
    private Publicacion publicacion;

    @Lob
    @JdbcTypeCode(SqlTypes.BLOB)
    @Column(name = "CONTENIDO", nullable = false)
    private byte[] contenido;

    @Column(name = "TIPO", nullable = false, length = 30)
    private String tipo;

    @Column(name = "ORDEN", nullable = false)
    private int orden;

    protected PublicacionImagen() {
    }

    public PublicacionImagen(Publicacion publicacion, byte[] contenido, String tipo, int orden) {
        this.publicacion = publicacion;
        this.contenido = contenido;
        this.tipo = tipo;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public byte[] getContenido() {
        return contenido;
    }

    public String getTipo() {
        return tipo;
    }

    public int getOrden() {
        return orden;
    }
}
