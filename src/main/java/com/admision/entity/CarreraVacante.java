package com.admision.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "carreras_vacantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarreraVacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "facultad", nullable = false, length = 150)
    private String facultad;

    @Column(name = "carrera", nullable = false, length = 150)
    private String carrera;

    @Column(name = "vacantes", nullable = false)
    private Integer vacantes;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();

        if (this.activo == null) {
            this.activo = true;
        }
    }
}