package com.admision.entity;

import com.admision.enums.EstadoProceso;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "procesos_admision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcesoAdmision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_proceso", nullable = false, length = 100)
    private String nombreProceso;

    @Column(name = "modalidad", nullable = false, length = 100)
    private String modalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoProceso estado;

    @Column(name = "total_postulantes")
    private Integer totalPostulantes;

    @Column(name = "total_ingresantes")
    private Integer totalIngresantes;

    @Column(name = "total_no_ingresantes")
    private Integer totalNoIngresantes;

    @Column(name = "codigo_verificacion", length = 50)
    private String codigoVerificacion;

    @Builder.Default
    @Column(name = "puntaje_correcta", precision = 10, scale = 4, nullable = false, columnDefinition = "numeric(10,4) default 20.0000")
    private BigDecimal puntajeCorrecta = new BigDecimal("20.0000");

    @Builder.Default
    @Column(name = "puntaje_incorrecta", precision = 10, scale = 4, nullable = false, columnDefinition = "numeric(10,4) default -1.2500")
    private BigDecimal puntajeIncorrecta = new BigDecimal("-1.2500");

    @Builder.Default
    @Column(name = "puntaje_blanca", precision = 10, scale = 4, nullable = false, columnDefinition = "numeric(10,4) default 1.2500")
    private BigDecimal puntajeBlanca = new BigDecimal("1.2500");

    @Builder.Default
    @Column(name = "factor_escala", precision = 10, scale = 4, nullable = false, columnDefinition = "numeric(10,4) default 100.0000")
    private BigDecimal factorEscala = new BigDecimal("100.0000");

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();

        if (this.estado == null) {
            this.estado = EstadoProceso.PENDIENTE;
        }

        if (this.totalPostulantes == null) {
            this.totalPostulantes = 0;
        }

        if (this.totalIngresantes == null) {
            this.totalIngresantes = 0;
        }

        if (this.totalNoIngresantes == null) {
            this.totalNoIngresantes = 0;
        }

        if (this.puntajeCorrecta == null) {
            this.puntajeCorrecta = new BigDecimal("20.0000");
        }

        if (this.puntajeIncorrecta == null) {
            this.puntajeIncorrecta = new BigDecimal("-1.2500");
        }

        if (this.puntajeBlanca == null) {
            this.puntajeBlanca = new BigDecimal("1.2500");
        }

        if (this.factorEscala == null) {
            this.factorEscala = new BigDecimal("100.0000");
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();

        if (this.puntajeCorrecta == null) {
            this.puntajeCorrecta = new BigDecimal("20.0000");
        }

        if (this.puntajeIncorrecta == null) {
            this.puntajeIncorrecta = new BigDecimal("-1.2500");
        }

        if (this.puntajeBlanca == null) {
            this.puntajeBlanca = new BigDecimal("1.2500");
        }

        if (this.factorEscala == null) {
            this.factorEscala = new BigDecimal("100.0000");
        }
    }
}