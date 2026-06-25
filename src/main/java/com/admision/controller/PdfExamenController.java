package com.admision.controller;

import com.admision.dto.pdf.PdfExamenResponse;
import com.admision.service.examen.PdfClaveExamenService;
import com.admision.service.examen.PdfExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PdfExamenController {

    private final PdfExamenService pdfExamenService;
    private final PdfClaveExamenService pdfClaveExamenService;

    @Value("${app.storage.pdfs}")
    private String carpetaPdfs;

    @PostMapping("/{examenId}/pdf/examenes")
    public ResponseEntity<List<PdfExamenResponse>> generarPdfExamenes(
            @PathVariable Long examenId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pdfExamenService.generarPdfExamenTodos(examenId));
    }

    @PostMapping("/{examenId}/temas/{letraTema}/pdf/examen")
    public ResponseEntity<PdfExamenResponse> generarPdfExamenTema(
            @PathVariable Long examenId,
            @PathVariable String letraTema) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pdfExamenService.generarPdfExamenTema(examenId, letraTema));
    }

    @PostMapping("/{examenId}/pdf/claves")
    public ResponseEntity<List<PdfExamenResponse>> generarPdfClaves(
            @PathVariable Long examenId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pdfClaveExamenService.generarPdfClaveTodos(examenId));
    }

    @PostMapping("/{examenId}/temas/{letraTema}/pdf/clave")
    public ResponseEntity<PdfExamenResponse> generarPdfClaveTema(
            @PathVariable Long examenId,
            @PathVariable String letraTema) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pdfClaveExamenService.generarPdfClaveTema(examenId, letraTema));
    }

    @GetMapping("/pdf/ver/{nombreArchivo}")
    public ResponseEntity<Resource> verPdf(@PathVariable String nombreArchivo) {
        return servirPdf(nombreArchivo, "inline");
    }

    @GetMapping("/pdf/descargar/{nombreArchivo}")
    public ResponseEntity<Resource> descargarPdf(@PathVariable String nombreArchivo) {
        return servirPdf(nombreArchivo, "attachment");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(IllegalArgumentException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", true);
        response.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarEstado(IllegalStateException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", true);
        response.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private ResponseEntity<Resource> servirPdf(String nombreArchivo, String disposicion) {
        try {
            validarNombreArchivo(nombreArchivo);
            Path ruta = Paths.get(carpetaPdfs).resolve(nombreArchivo).normalize();
            if (!Files.exists(ruta)) {
                throw new RuntimeException("El archivo PDF no existe: " + nombreArchivo);
            }
            Resource recurso = new UrlResource(ruta.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            disposicion + "; filename=\"" + nombreArchivo + "\"")
                    .body(recurso);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error al servir PDF: " + e.getMessage(), e);
        }
    }

    private void validarNombreArchivo(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("Nombre de archivo inválido.");
        }
        if (nombreArchivo.contains("..") || nombreArchivo.contains("/") || nombreArchivo.contains("\\")) {
            throw new IllegalArgumentException("Nombre de archivo no permitido.");
        }
        if (!nombreArchivo.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF.");
        }
    }
}
