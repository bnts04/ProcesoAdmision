package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class PuntajePostulanteResponse {

    private String codigo;
    private String litho;
    private String tema;
    private String secuencia;

    private Integer correctas;
    private Integer incorrectas;
    private Integer blancas;

    private BigDecimal puntajeBruto;
    private BigDecimal puntajeFinal;

    private Boolean puntajeCalculado;
    private String observacion;
}