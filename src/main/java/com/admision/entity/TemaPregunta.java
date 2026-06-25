package com.admision.entity;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.LetraAlternativa;
import com.admision.enums.SubcursoPregunta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "temas_preguntas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tema_numero_pregunta",
                        columnNames = {"tema_id", "numero_pregunta"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemaPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tema_id", nullable = false)
    private TemaExamen tema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pregunta_origen_id", nullable = false)
    private PreguntaBanco preguntaOrigen;

    @Column(name = "numero_pregunta", nullable = false)
    private Integer numeroPregunta;

    @Column(name = "codigo_pregunta", nullable = false, length = 20)
    private String codigoPregunta;

    @Enumerated(EnumType.STRING)
    @Column(name = "componente", nullable = false, length = 50)
    private ComponentePregunta componente;

    @Enumerated(EnumType.STRING)
    @Column(name = "subcurso", nullable = false, length = 50)
    private SubcursoPregunta subcurso;

    @Column(name = "enunciado", nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "respuesta_correcta_final", nullable = false, length = 1)
    private LetraAlternativa respuestaCorrectaFinal;
}