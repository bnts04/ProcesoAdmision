package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class EstadisticaProcesoResponse {

    private Long procesoId;

    private Long totalPostulantes;

    private BigDecimal mayorPuntaje;
    private BigDecimal menorPuntaje;
    private BigDecimal promedioPuntaje;

    private BigDecimal promedioCorrectas;
    private BigDecimal promedioIncorrectas;
    private BigDecimal promedioBlancas;
}