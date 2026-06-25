package com.admision.entity;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.SubcursoPregunta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "configuraciones_area_examen",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_area_subcurso",
                        columnNames = {"area_id", "subcurso"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionAreaExamen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private AreaExamen area;

    @Enumerated(EnumType.STRING)
    @Column(name = "componente", nullable = false, length = 50)
    private ComponentePregunta componente;

    @Enumerated(EnumType.STRING)
    @Column(name = "subcurso", nullable = false, length = 50)
    private SubcursoPregunta subcurso;

    @Column(name = "cantidad_preguntas", nullable = false)
    private Integer cantidadPreguntas;
}