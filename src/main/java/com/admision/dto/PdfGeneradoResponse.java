package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PdfGeneradoResponse {

    private Long procesoId;
    private String tipoPdf;

    private String nombreArchivo;
    private String rutaArchivo;

    private String urlVer;
    private String urlDescargar;

    private String mensaje;
}