package com.admision.dto;

import com.admision.entity.PdfGenerado;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PdfHistorialResponse {

    private Long id;
    private Long procesoId;

    private String tipoPdf;
    private String nombreArchivo;
    private String rutaArchivo;
    private String carrera;

    private String urlVer;
    private String urlDescargar;

    private LocalDateTime fechaGeneracion;

    public static PdfHistorialResponse fromEntity(PdfGenerado pdf) {
        String nombreArchivo = pdf.getNombreArchivo();

        return PdfHistorialResponse.builder()
                .id(pdf.getId())
                .procesoId(pdf.getProceso().getId())
                .tipoPdf(pdf.getTipoPdf())
                .nombreArchivo(nombreArchivo)
                .rutaArchivo(pdf.getRutaArchivo())
                .carrera(pdf.getCarrera())
                .urlVer("/api/pdf/ver/" + nombreArchivo)
                .urlDescargar("/api/pdf/descargar/" + nombreArchivo)
                .fechaGeneracion(pdf.getFechaGeneracion())
                .build();
    }
}