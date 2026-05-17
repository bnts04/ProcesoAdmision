package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrdenMeritoResponse {

    private Long procesoId;
    private Integer totalResultados;
    private Integer totalConOmg;
    private Integer totalConOme;
    private String mensaje;
}