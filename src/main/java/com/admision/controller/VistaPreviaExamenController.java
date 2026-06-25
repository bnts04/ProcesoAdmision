package com.admision.controller;

import com.admision.dto.vistaprevia.VistaPreviaExamenResponse;
import com.admision.service.examen.VistaPreviaExamenService;
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
public class VistaPreviaExamenController {

    private final VistaPreviaExamenService vistaPreviaExamenService;

    @GetMapping("/{examenId}/temas/{letraTema}/vista-previa")
    public ResponseEntity<VistaPreviaExamenResponse> vistaPreviaTema(
            @PathVariable Long examenId,
            @PathVariable String letraTema) {
        return ResponseEntity.ok(vistaPreviaExamenService.obtenerVistaPreviaTema(examenId, letraTema));
    }

    @GetMapping("/{examenId}/vista-previa")
    public ResponseEntity<List<VistaPreviaExamenResponse>> vistaPreviaCompleta(
            @PathVariable Long examenId) {
        return ResponseEntity.ok(vistaPreviaExamenService.obtenerVistaPreviaCompleta(examenId));
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
        return ResponseEntity.badRequest().body(response);
    }
}
