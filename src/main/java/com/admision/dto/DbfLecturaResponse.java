package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DbfLecturaResponse {

    private Long procesoId;
    private String tipoArchivo;
    private String nombreArchivo;
    private Integer totalColumnas;
    private Integer totalRegistrosLeidos;
    private List<String> columnas;
    private List<Map<String, String>> registros;
}