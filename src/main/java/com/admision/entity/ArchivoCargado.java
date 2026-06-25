package com.admision.entity;

import com.admision.enums.EstadoValidacion;
import com.admision.enums.TipoArchivo;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "archivos_cargados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchivoCargado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proceso_id", nullable = false)
    private ProcesoAdmision proceso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_archivo", nullable = false, length = 40)
    private TipoArchivo tipoArchivo;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "nombre_guardado", nullable = false, length = 255)
    private String nombreGuardado;

    @Column(name = "ruta_archivo", nullable = false, length = 500)
    private String rutaArchivo;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion", nullable = false, length = 30)
    private EstadoValidacion estadoValidacion;

    @Column(name = "observacion", length = 500)
    private String observacion;

    @Column(name = "fecha_carga", nullable = false)
    private LocalDateTime fechaCarga;

    @PrePersist
    public void prePersist() {
        this.fechaCarga = LocalDateTime.now();

        if (this.estadoValidacion == null) {
            this.estadoValidacion = EstadoValidacion.PENDIENTE;
        }
    }
}