package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ResumenCarreraResponse {

    private String facultad;
    private String carrera;

    private Integer vacantes;
    private Integer totalPostulantes;
    private Integer totalIngresantes;
    private Integer totalNoIngresantes;
    private Integer totalPendientes;

    private BigDecimal mayorPuntaje;
    private BigDecimal menorPuntaje;
    private BigDecimal promedioPuntaje;
}