package com.admision.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "examenes_preguntas_base",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_examen_pregunta_base",
                        columnNames = {"examen_id", "pregunta_id"}
                ),
                @UniqueConstraint(
                        name = "uk_examen_orden_base",
                        columnNames = {"examen_id", "orden_base"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenPreguntaBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "examen_id", nullable = false)
    private ExamenGenerado examen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private PreguntaBanco pregunta;

    @Column(name = "orden_base", nullable = false)
    private Integer ordenBase;
}