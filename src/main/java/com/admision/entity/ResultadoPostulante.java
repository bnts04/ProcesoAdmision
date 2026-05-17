package com.admision.entity;

import com.admision.enums.CondicionPostulante;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "resultados_postulantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoPostulante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private ProcesoAdmision proceso;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "dni", length = 20)
    private String dni;

    @Column(name = "apellidos_nombres", length = 255)
    private String apellidosNombres;

    @Column(name = "facultad", length = 150)
    private String facultad;

    @Column(name = "carrera", length = 150)
    private String carrera;

    @Column(name = "litho", length = 20)
    private String litho;

    @Column(name = "tema", length = 10)
    private String tema;

    @Column(name = "secuencia", length = 20)
    private String secuencia;

    @Column(name = "correctas")
    private Integer correctas;

    @Column(name = "incorrectas")
    private Integer incorrectas;

    @Column(name = "blancas")
    private Integer blancas;

    @Column(name = "puntaje_bruto", precision = 10, scale = 2)
    private BigDecimal puntajeBruto;

    @Column(name = "puntaje_final", precision = 10, scale = 4)
    private BigDecimal puntajeFinal;

    @Column(name = "ome")
    private Integer ome;

    @Column(name = "omg")
    private Integer omg;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion", nullable = false, length = 30)
    private CondicionPostulante condicion;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void prePersist() {
        this.fechaRegistro = LocalDateTime.now();

        if (this.condicion == null) {
            this.condicion = CondicionPostulante.PENDIENTE;
        }
    }
}