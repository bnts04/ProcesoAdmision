package com.admision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vacantes_proceso",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_vacante_proceso_carrera",
                        columnNames = {"proceso_id", "carrera"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacanteProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private ProcesoAdmision proceso;

    @Column(name = "facultad", nullable = false, length = 150)
    private String facultad;

    @Column(name = "carrera", nullable = false, length = 150)
    private String carrera;

    @Column(name = "vacantes", nullable = false)
    private Integer vacantes;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}