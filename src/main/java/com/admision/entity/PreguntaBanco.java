package com.admision.entity;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.enums.SubcursoPregunta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "preguntas_banco")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaBanco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true, length = 20)
    private String codigo;

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

    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPregunta estado;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("letraOriginal ASC")
    @Builder.Default
    private List<AlternativaPregunta> alternativas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();

        if (this.estado == null) {
            this.estado = EstadoPregunta.ACTIVA;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}