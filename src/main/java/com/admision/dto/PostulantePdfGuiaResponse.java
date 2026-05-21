package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostulantePdfGuiaResponse {

    private String codigo;
    private String nombre;

    private String facultad;
    private String carrera;

    private String puntajePdf;
    private String meritoPdf;
    private String condicionPdf;
    private String secuenciaPdf;
}