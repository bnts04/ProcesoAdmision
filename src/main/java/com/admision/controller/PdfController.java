package com.admision.controller;

import com.admision.dto.PdfGeneradoResponse;
import com.admision.service.pdf.PdfResultadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.admision.dto.PdfHistorialResponse;
import com.admision.repository.PdfGeneradoRepository;

import java.util.List;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfResultadoService pdfResultadoService;
    private final PdfGeneradoRepository pdfGeneradoRepository;

    @Value("${app.storage.pdfs}")
    private String carpetaPdfs;

    @PostMapping("/proceso/{procesoId}/general")
    public PdfGeneradoResponse generarPdfGeneral(@PathVariable Long procesoId) {
        return pdfResultadoService.generarPdfGeneral(procesoId);
    }

    @GetMapping("/proceso/{procesoId}/historial")
    public List<PdfHistorialResponse> listarHistorialPdf(@PathVariable Long procesoId) {
        return pdfGeneradoRepository.findByProcesoIdOrderByFechaGeneracionDesc(procesoId)
                .stream()
                .map(PdfHistorialResponse::fromEntity)
                .toList();
    }

    @PostMapping("/proceso/{procesoId}/carrera")
    public PdfGeneradoResponse generarPdfPorCarrera(
            @PathVariable Long procesoId,
            @RequestParam String nombre
    ) {
        return pdfResultadoService.generarPdfPorCarrera(procesoId, nombre);
    }

    @GetMapping("/descargar/{nombreArchivo}")
    public ResponseEntity<Resource> descargarPdf(@PathVariable String nombreArchivo) {
        try {
            validarNombreArchivo(nombreArchivo);

            Path ruta = Paths.get(carpetaPdfs).resolve(nombreArchivo).normalize();

            if (!Files.exists(ruta)) {
                throw new RuntimeException("El archivo PDF no existe: " + nombreArchivo);
            }

            Resource recurso = new UrlResource(ruta.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + nombreArchivo + "\""
                    )
                    .body(recurso);

        } catch (Exception e) {
            throw new RuntimeException("Error al descargar PDF: " + e.getMessage(), e);
        }
    }

    @GetMapping("/ver/{nombreArchivo}")
    public ResponseEntity<Resource> verPdf(@PathVariable String nombreArchivo) {
        try {
            validarNombreArchivo(nombreArchivo);

            Path ruta = Paths.get(carpetaPdfs).resolve(nombreArchivo).normalize();

            if (!Files.exists(ruta)) {
                throw new RuntimeException("El archivo PDF no existe: " + nombreArchivo);
            }

            Resource recurso = new UrlResource(ruta.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + nombreArchivo + "\""
                    )
                    .body(recurso);

        } catch (Exception e) {
            throw new RuntimeException("Error al visualizar PDF: " + e.getMessage(), e);
        }
    }

    private void validarNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new RuntimeException("Nombre de archivo inválido");
        }

        if (nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
            throw new RuntimeException("Nombre de archivo no permitido");
        }

        if (!nombreArchivo.toLowerCase().endsWith(".pdf")) {
            throw new RuntimeException("Solo se permite descargar archivos PDF");
        }
    }
}