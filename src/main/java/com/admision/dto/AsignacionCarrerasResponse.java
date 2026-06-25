package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AsignacionCarrerasResponse {

    private Long procesoId;
    private Integer totalResultados;
    private Integer totalPadron;
    private Integer totalActualizados;
    private Integer totalNoEncontrados;
    private List<String> codigosNoEncontrados;
    private String mensaje;
}