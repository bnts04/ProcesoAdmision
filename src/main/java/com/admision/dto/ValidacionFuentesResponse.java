package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidacionFuentesResponse {

    private Long procesoId;
    private Boolean valido;

    private Integer totalFuentesRevisadas;
    private Integer totalFuentesValidas;
    private Integer totalFuentesConError;

    private List<DetalleValidacionFuenteResponse> detalles;

    private String mensaje;
}