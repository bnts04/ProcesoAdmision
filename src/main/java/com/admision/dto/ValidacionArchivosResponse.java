package com.admision.dto;

import com.admision.enums.EstadoProceso;
import com.admision.enums.TipoArchivo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidacionArchivosResponse {

    private Long procesoId;
    private boolean valido;
    private String mensaje;
    private List<TipoArchivo> archivosObligatorios;
    private List<String> faltantes;
    private Integer totalArchivosCargados;
    private EstadoProceso estadoProceso;
}