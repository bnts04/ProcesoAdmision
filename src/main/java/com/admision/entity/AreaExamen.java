package com.admision.entity;

import com.admision.enums.CodigoAreaExamen;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "areas_examen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AreaExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private CodigoAreaExamen codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}