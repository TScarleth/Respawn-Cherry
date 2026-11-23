package com.respawncherry.foro.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "DENUNCIAS", uniqueConstraints = @UniqueConstraint(columnNames = {"PUBLICACION_ID", "DENUNCIANTE_ID"}))
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "denuncia_seq")
    @SequenceGenerator(name = "denuncia_seq", sequenceName = "DENUNCIA_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PUBLICACION_ID", nullable = false)
    private Publicacion publicacion;

    @Column(name = "DENUNCIANTE_ID", nullable = false)
    private Long denuncianteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoDenuncia motivo;

    @Column(length = 300)
    private String explicacion;

    @Column(length = 500)
    private String detalle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDenuncia estado;

    @Column(name = "FECHA_CREACION", nullable = false)
    private Instant fechaCreacion;

    protected Denuncia() {
    }

    public Denuncia(Publicacion publicacion, Long denuncianteId, MotivoDenuncia motivo, String explicacion, String detalle) {
        this.publicacion = publicacion;
        this.denuncianteId = denuncianteId;
        this.motivo = motivo;
        this.explicacion = explicacion;
        this.detalle = detalle;
        this.estado = EstadoDenuncia.PENDIENTE;
    }

    @PrePersist
    void alCrear() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
        if (estado == null) {
            estado = EstadoDenuncia.PENDIENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public Publicacion getPublicacion() {
        return publicacion;
    }

    public Long getDenuncianteId() {
        return denuncianteId;
    }

    public MotivoDenuncia getMotivo() {
        return motivo;
    }

    public String getExplicacion() {
        return explicacion;
    }

    public String getDetalle() {
        return detalle;
    }

    public EstadoDenuncia getEstado() {
        return estado;
    }

    public void setEstado(EstadoDenuncia estado) {
        this.estado = estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }
}
