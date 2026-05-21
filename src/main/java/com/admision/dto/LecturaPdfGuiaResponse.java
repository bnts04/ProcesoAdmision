package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LecturaPdfGuiaResponse {

    private Long procesoId;
    private Long procesoPdfUsado;

    private String nombreArchivo;

    private Boolean pdfDelProcesoActual;
    private Boolean pdfGlobalUtilizado;

    private Integer totalRegistrosLeidos;
    private Integer totalMostrados;

    private List<PostulantePdfGuiaResponse> registros;

    private String mensaje;
}