package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class RespuestaPostulanteExcelResponse {

    private String litho;
    private String tema;
    private Integer totalPreguntas;
    private Map<String, String> respuestas;
}