package com.admision.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CrearProcesoRequest {

    @NotBlank(message = "El nombre del proceso es obligatorio")
    private String nombreProceso;

    @NotBlank(message = "La modalidad es obligatoria")
    private String modalidad;

    private BigDecimal puntajeCorrecta;

    private BigDecimal puntajeIncorrecta;

    private BigDecimal puntajeBlanca;

    private BigDecimal factorEscala;
}