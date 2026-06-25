package com.admision.controller;

import com.admision.dto.historial.DetalleExamenGeneradoResponse;
import com.admision.dto.historial.HistorialExamenResponse;
import com.admision.dto.historial.PdfGeneradoHistorialResponse;
import com.admision.dto.tema.TemaExamenResponse;
import com.admision.service.examen.HistorialExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HistorialExamenController {

    private final HistorialExamenService historialExamenService;

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialExamenResponse>> listarHistorial() {
        return ResponseEntity.ok(historialExamenService.listarHistorial());
    }

    @GetMapping("/{examenId}/detalle")
    public ResponseEntity<DetalleExamenGeneradoResponse> obtenerDetalle(
            @PathVariable Long examenId
    ) {
        return ResponseEntity.ok(historialExamenService.obtenerDetalle(examenId));
    }

    @GetMapping("/{examenId}/pdfs")
    public ResponseEntity<List<PdfGeneradoHistorialResponse>> listarPdfs(
            @PathVariable Long examenId
    ) {
        return ResponseEntity.ok(historialExamenService.listarPdfsExamen(examenId));
    }

    @GetMapping("/{examenId}/temas-resumen")
    public ResponseEntity<List<TemaExamenResponse>> listarTemas(
            @PathVariable Long examenId
    ) {
        return ResponseEntity.ok(historialExamenService.listarTemasExamen(examenId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(IllegalArgumentException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", true);
        response.put("mensaje", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
