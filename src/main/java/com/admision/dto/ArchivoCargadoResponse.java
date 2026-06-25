package com.admision.dto;

import com.admision.entity.ArchivoCargado;
import com.admision.enums.EstadoValidacion;
import com.admision.enums.TipoArchivo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ArchivoCargadoResponse {

    private Long id;
    private Long procesoId;
    private TipoArchivo tipoArchivo;
    private String nombreOriginal;
    private String nombreGuardado;
    private String rutaArchivo;
    private String contentType;
    private Long tamanoBytes;
    private EstadoValidacion estadoValidacion;
    private String observacion;
    private LocalDateTime fechaCarga;

    public static ArchivoCargadoResponse fromEntity(ArchivoCargado archivo) {
        return ArchivoCargadoResponse.builder()
                .id(archivo.getId())
                .procesoId(archivo.getProceso().getId())
                .tipoArchivo(archivo.getTipoArchivo())
                .nombreOriginal(archivo.getNombreOriginal())
                .nombreGuardado(archivo.getNombreGuardado())
                .rutaArchivo(archivo.getRutaArchivo())
                .contentType(archivo.getContentType())
                .tamanoBytes(archivo.getTamanoBytes())
                .estadoValidacion(archivo.getEstadoValidacion())
                .observacion(archivo.getObservacion())
                .fechaCarga(archivo.getFechaCarga())
                .build();
    }
}
