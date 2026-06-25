package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentificacionPostulanteExcelResponse {

    private String litho;
    private String tema;
    private String codigo;
    private String secuencia;
}