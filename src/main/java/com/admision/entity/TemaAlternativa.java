package com.admision.entity;

import com.admision.enums.LetraAlternativa;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "temas_alternativas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tema_pregunta_letra_final",
                        columnNames = {"tema_pregunta_id", "letra_final"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemaAlternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_pregunta_id", nullable = false)
    private TemaPregunta temaPregunta;

    @Enumerated(EnumType.STRING)
    @Column(name = "letra_original", nullable = false, length = 1)
    private LetraAlternativa letraOriginal;

    @Enumerated(EnumType.STRING)
    @Column(name = "letra_final", nullable = false, length = 1)
    private LetraAlternativa letraFinal;

    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta;
}