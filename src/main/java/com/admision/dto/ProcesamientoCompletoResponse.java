package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProcesamientoCompletoResponse {

    private Long procesoId;

    private Boolean ejecutadoCorrectamente;

    private ValidacionFuentesResponse validacionFuentes;
    private ActualizacionDesdePdfResponse actualizacionDesdePdf;
    private CondicionIngresoResponse condicionIngreso;
    private VistaPreviaFinalResponse vistaPreviaFinal;

    private List<String> pasosEjecutados;
    private List<String> observaciones;

    private String mensaje;
}