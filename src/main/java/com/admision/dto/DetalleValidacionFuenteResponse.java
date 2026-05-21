package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DetalleValidacionFuenteResponse {

    private String tipoArchivo;
    private String nombreArchivo;
    private Long procesoArchivoUsado;

    private Boolean valido;
    private Boolean pdfGlobalUtilizado;

    private Integer totalColumnas;
    private Integer totalRegistros;

    private List<String> observaciones;
}