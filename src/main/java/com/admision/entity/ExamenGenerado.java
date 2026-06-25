package com.admision.entity;

import com.admision.enums.EstadoExamenGenerado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "examenes_generados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_examen", nullable = false, length = 200)
    private String nombreExamen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private AreaExamen area;

    @Column(name = "cantidad_temas", nullable = false)
    private Integer cantidadTemas;

    @Column(name = "tema_inicial", nullable = false, length = 1)
    private String temaInicial;

    @Column(name = "total_preguntas", nullable = false)
    private Integer totalPreguntas;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoExamenGenerado estado;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @PrePersist
    public void prePersist() {
        if (fechaGeneracion == null) {
            fechaGeneracion = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoExamenGenerado.BASE_GENERADA;
        }
    }
}