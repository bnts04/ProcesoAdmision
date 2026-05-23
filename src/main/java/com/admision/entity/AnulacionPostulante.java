package com.admision.entity;

import com.admision.enums.CondicionPostulante;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "anulaciones_postulante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnulacionPostulante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private ProcesoAdmision proceso;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resultado_postulante_id", nullable = false, unique = true)
    private ResultadoPostulante resultadoPostulante;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "motivo", nullable = false, length = 1000)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_anterior", length = 30)
    private CondicionPostulante condicionAnterior;

    @Column(name = "puntaje_final_anterior", precision = 10, scale = 4)
    private BigDecimal puntajeFinalAnterior;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(name = "fecha_anulacion", nullable = false)
    private LocalDateTime fechaAnulacion;

    @PrePersist
    public void prePersist() {
        this.fechaAnulacion = LocalDateTime.now();
    }
}