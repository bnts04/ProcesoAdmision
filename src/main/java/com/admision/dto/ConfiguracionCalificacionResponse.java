package com.admision.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ConfiguracionCalificacionResponse {

    private Long procesoId;

    private String nombreProceso;

    private String modalidad;

    private String estadoProceso;

    private BigDecimal puntajeCorrecta;

    private BigDecimal puntajeIncorrecta;

    private BigDecimal puntajeBlanca;

    private BigDecimal factorEscala;

    private String mensaje;
}