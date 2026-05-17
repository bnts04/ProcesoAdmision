package com.admision.controller;

import com.admision.dto.ArchivoCargadoResponse;
import com.admision.enums.TipoArchivo;
import com.admision.service.ArchivoCargadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.admision.dto.ValidacionArchivosResponse;

import java.util.List;

@RestController
@RequestMapping("/api/archivos")
@RequiredArgsConstructor
public class ArchivoCargadoController {

    private final ArchivoCargadoService archivoCargadoService;

    @PostMapping("/cargar/{procesoId}")
    public ArchivoCargadoResponse cargarArchivo(
            @PathVariable Long procesoId,
            @RequestParam("tipoArchivo") TipoArchivo tipoArchivo,
            @RequestParam("archivo") MultipartFile archivo
    ) {
        return archivoCargadoService.cargarArchivo(procesoId, tipoArchivo, archivo);
    }

    @GetMapping("/proceso/{procesoId}")
    public List<ArchivoCargadoResponse> listarArchivosPorProceso(@PathVariable Long procesoId) {
        return archivoCargadoService.listarArchivosPorProceso(procesoId);
    }

    @PostMapping("/validar/{procesoId}")
    public ValidacionArchivosResponse validarArchivosObligatorios(@PathVariable Long procesoId) {
        return archivoCargadoService.validarArchivosObligatorios(procesoId);
    }
}