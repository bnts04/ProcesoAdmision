package com.admision.controller;

import com.admision.dto.AnulacionPostulanteResponse;
import com.admision.service.AnulacionPostulanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/anulaciones-postulante")
@RequiredArgsConstructor
public class AnulacionPostulanteController {

    private final AnulacionPostulanteService anulacionPostulanteService;

    @Value("${app.storage.evidencias}")
    private String carpetaEvidencias;

    @PostMapping
    public AnulacionPostulanteResponse anularPostulante(
            @RequestParam Long procesoId,
            @RequestParam String codigo,
            @RequestParam String motivo,
            @RequestParam MultipartFile evidencia
    ) {
        return anulacionPostulanteService.anularPostulante(
                procesoId,
                codigo,
                motivo,
                evidencia
        );
    }

    @GetMapping("/proceso/{procesoId}")
    public List<AnulacionPostulanteResponse> listarAnulacionesPorProceso(@PathVariable Long procesoId) {
        return anulacionPostulanteService.listarAnulacionesPorProceso(procesoId);
    }

    @GetMapping("/proceso/{procesoId}/codigo/{codigo}")
    public AnulacionPostulanteResponse obtenerAnulacionPorCodigo(
            @PathVariable Long procesoId,
            @PathVariable String codigo
    ) {
        return anulacionPostulanteService.obtenerAnulacionPorCodigo(procesoId, codigo);
    }

    @GetMapping("/evidencia/ver/{nombreArchivo}")
    public ResponseEntity<Resource> verEvidencia(@PathVariable String nombreArchivo) {
        return obtenerArchivoEvidencia(nombreArchivo, false);
    }

    @GetMapping("/evidencia/descargar/{nombreArchivo}")
    public ResponseEntity<Resource> descargarEvidencia(@PathVariable String nombreArchivo) {
        return obtenerArchivoEvidencia(nombreArchivo, true);
    }

    private ResponseEntity<Resource> obtenerArchivoEvidencia(String nombreArchivo, boolean descargar) {
        try {
            validarNombreArchivo(nombreArchivo);

            Path ruta = Paths.get(carpetaEvidencias).resolve(nombreArchivo).normalize();

            if (!Files.exists(ruta)) {
                throw new RuntimeException("La evidencia no existe: " + nombreArchivo);
            }

            Resource recurso = new UrlResource(ruta.toUri());

            String contentType = Files.probeContentType(ruta);

            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            String disposition = descargar ? "attachment" : "inline";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            disposition + "; filename=\"" + nombreArchivo + "\""
                    )
                    .body(recurso);

        } catch (Exception e) {
            throw new RuntimeException("Error al obtener evidencia: " + e.getMessage(), e);
        }
    }

    private void validarNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new RuntimeException("Nombre de archivo inválido");
        }

        if (nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
            throw new RuntimeException("Nombre de archivo no permitido");
        }
    }
}