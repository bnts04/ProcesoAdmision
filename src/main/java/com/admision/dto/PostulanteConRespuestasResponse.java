package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class PostulanteConRespuestasResponse {

    private String codigo;
    private String litho;

    /*
     * Este es el TEMA final que usará el sistema para calcular.
     * Puede venir de RESPUEST o de IDENTIFI.
     */
    private String tema;

    private String temaRespuest;
    private String temaIdentifi;

    private String secuencia;
    private Integer totalPreguntas;
    private Map<String, String> respuestas;

    private Boolean temaValido;
    private String observacionTema;
}