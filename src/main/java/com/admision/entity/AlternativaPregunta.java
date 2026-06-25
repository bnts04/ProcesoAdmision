package com.admision.entity;

import com.admision.enums.LetraAlternativa;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alternativas_pregunta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlternativaPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    private PreguntaBanco pregunta;

    @Enumerated(EnumType.STRING)
    @Column(name = "letra_original", nullable = false, length = 1)
    private LetraAlternativa letraOriginal;

    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "es_correcta", nullable = false)
    private Boolean esCorrecta;
}