package com.admision.entity;

import com.admision.enums.EstadoProceso;
import jakarta.persistence.*;
import lombok.*;

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
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}