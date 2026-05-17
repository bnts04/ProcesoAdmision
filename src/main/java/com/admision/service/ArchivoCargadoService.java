package com.admision.service;

import com.admision.dto.ArchivoCargadoResponse;
import com.admision.entity.ArchivoCargado;
import com.admision.entity.ProcesoAdmision;
import com.admision.enums.EstadoProceso;
import com.admision.enums.EstadoValidacion;
import com.admision.enums.TipoArchivo;
import com.admision.repository.ArchivoCargadoRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.admision.dto.ValidacionArchivosResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArchivoCargadoService {

    private final ArchivoCargadoRepository archivoCargadoRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    @Value("${app.storage.excels}")
    private String carpetaExcels;

    public ArchivoCargadoResponse cargarArchivo(Long procesoId, TipoArchivo tipoArchivo, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new RuntimeException("Debe seleccionar un archivo");
        }

        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        try {
            Path carpetaDestino = Paths.get(carpetaExcels);
            Files.createDirectories(carpetaDestino);

            String nombreOriginal = archivo.getOriginalFilename();

            if (nombreOriginal == null || nombreOriginal.isBlank()) {
                throw new RuntimeException("El archivo no tiene nombre válido");
            }

            String extension = obtenerExtension(nombreOriginal);
            String nombreGuardado = tipoArchivo.name() + "_" + UUID.randomUUID() + extension;

            Path rutaDestino = carpetaDestino.resolve(nombreGuardado);

            Files.copy(archivo.getInputStream(), rutaDestino);

            ArchivoCargado archivoCargado = ArchivoCargado.builder()
                    .proceso(proceso)
                    .tipoArchivo(tipoArchivo)
                    .nombreOriginal(nombreOriginal)
                    .nombreGuardado(nombreGuardado)
                    .rutaArchivo(rutaDestino.toString())
                    .contentType(archivo.getContentType())
                    .tamanoBytes(archivo.getSize())
                    .estadoValidacion(EstadoValidacion.PENDIENTE)
                    .observacion("Archivo cargado correctamente")
                    .build();

            proceso.setEstado(EstadoProceso.ARCHIVOS_CARGADOS);
            procesoAdmisionRepository.save(proceso);

            ArchivoCargado guardado = archivoCargadoRepository.save(archivoCargado);

            return ArchivoCargadoResponse.fromEntity(guardado);

        } catch (Exception e) {
            throw new RuntimeException("Error al cargar archivo: " + e.getMessage());
        }
    }

    public List<ArchivoCargadoResponse> listarArchivosPorProceso(Long procesoId) {
        return archivoCargadoRepository.findByProcesoId(procesoId)
                .stream()
                .map(ArchivoCargadoResponse::fromEntity)
                .toList();
    }

    private String obtenerExtension(String nombreArchivo) {
        int punto = nombreArchivo.lastIndexOf(".");
        if (punto == -1) {
            return "";
        }
        return nombreArchivo.substring(punto);
    }
    public ValidacionArchivosResponse validarArchivosObligatorios(Long procesoId) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        List<TipoArchivo> obligatorios = Arrays.asList(
                TipoArchivo.CLAVES,
                TipoArchivo.RESPUEST,
                TipoArchivo.IDENTIFI
        );

        List<ArchivoCargado> archivos = archivoCargadoRepository.findByProcesoId(procesoId);
        List<String> faltantes = new ArrayList<>();

        for (TipoArchivo tipoObligatorio : obligatorios) {
            List<ArchivoCargado> archivosDelTipo = archivos.stream()
                    .filter(a -> a.getTipoArchivo() == tipoObligatorio)
                    .toList();

            if (archivosDelTipo.isEmpty()) {
                faltantes.add(tipoObligatorio.name());
                continue;
            }

            boolean existeFisicamente = archivosDelTipo.stream()
                    .anyMatch(a -> Files.exists(Paths.get(a.getRutaArchivo())));

            if (!existeFisicamente) {
                faltantes.add(tipoObligatorio.name() + " (archivo físico no encontrado)");
            } else {
                archivosDelTipo.forEach(a -> {
                    a.setEstadoValidacion(EstadoValidacion.VALIDADO);
                    a.setObservacion("Archivo obligatorio validado correctamente");
                });

                archivoCargadoRepository.saveAll(archivosDelTipo);
            }
        }

        boolean valido = faltantes.isEmpty();

        if (valido) {
            proceso.setEstado(EstadoProceso.VALIDADO);
        } else {
            proceso.setEstado(EstadoProceso.CON_ADVERTENCIAS);
        }

        procesoAdmisionRepository.save(proceso);

        return ValidacionArchivosResponse.builder()
                .procesoId(proceso.getId())
                .valido(valido)
                .mensaje(valido
                        ? "Los archivos obligatorios fueron validados correctamente"
                        : "Faltan archivos obligatorios o no se encontraron físicamente")
                .archivosObligatorios(obligatorios)
                .faltantes(faltantes)
                .totalArchivosCargados(archivos.size())
                .estadoProceso(proceso.getEstado())
                .build();
    }
}