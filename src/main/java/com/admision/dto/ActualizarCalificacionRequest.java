package com.admision.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ActualizarCalificacionRequest {

    private BigDecimal puntajeCorrecta;

    private BigDecimal puntajeIncorrecta;

    private BigDecimal puntajeBlanca;

    private BigDecimal factorEscala;
}