package com.admision.dto;

import com.admision.entity.AnulacionPostulante;
import com.admision.enums.CondicionPostulante;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AnulacionPostulanteResponse {

    private Long id;
    private Long procesoId;

    private Long resultadoPostulanteId;
    private String codigo;
    private String apellidosNombres;
    private String facultad;
    private String carrera;

    private String motivo;

    private CondicionPostulante condicionAnterior;
    private CondicionPostulante condicionActual;

    private BigDecimal puntajeFinalAnterior;

    private String nombreArchivo;
    private String nombreOriginal;
    private String rutaArchivo;

    private String urlVerEvidencia;
    private String urlDescargarEvidencia;

    private LocalDateTime fechaAnulacion;

    private String mensaje;

    public static AnulacionPostulanteResponse fromEntity(
            AnulacionPostulante anulacion,
            String mensaje
    ) {
        String nombreArchivo = anulacion.getNombreArchivo();

        return AnulacionPostulanteResponse.builder()
                .id(anulacion.getId())
                .procesoId(anulacion.getProceso().getId())
                .resultadoPostulanteId(anulacion.getResultadoPostulante().getId())
                .codigo(anulacion.getCodigo())
                .apellidosNombres(anulacion.getResultadoPostulante().getApellidosNombres())
                .facultad(anulacion.getResultadoPostulante().getFacultad())
                .carrera(anulacion.getResultadoPostulante().getCarrera())
                .motivo(anulacion.getMotivo())
                .condicionAnterior(anulacion.getCondicionAnterior())
                .condicionActual(anulacion.getResultadoPostulante().getCondicion())
                .puntajeFinalAnterior(anulacion.getPuntajeFinalAnterior())
                .nombreArchivo(nombreArchivo)
                .nombreOriginal(anulacion.getNombreOriginal())
                .rutaArchivo(anulacion.getRutaArchivo())
                .urlVerEvidencia("/api/anulaciones-postulante/evidencia/ver/" + nombreArchivo)
                .urlDescargarEvidencia("/api/anulaciones-postulante/evidencia/descargar/" + nombreArchivo)
                .fechaAnulacion(anulacion.getFechaAnulacion())
                .mensaje(mensaje)
                .build();
    }
}