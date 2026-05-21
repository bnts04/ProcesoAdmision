package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DiagnosticoTemaResponse {

    private Long procesoId;

    private Integer totalRegistros;

    private Integer temasValidos;
    private Integer temasInvalidos;

    private Integer temaValidadoEnAmbos;
    private Integer temaTomadoDesdeRespuest;
    private Integer temaTomadoDesdeIdentifi;

    private Integer conflictosTema;
    private Integer sinTemaEnAmbos;
    private Integer lithoNoEncontrado;

    private List<String> observaciones;

    private String mensaje;
}