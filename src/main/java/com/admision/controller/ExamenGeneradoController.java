package com.admision.controller;

import com.admision.dto.examen.DetalleExamenBaseResponse;
import com.admision.dto.examen.ExamenGeneradoResponse;
import com.admision.dto.examen.GenerarExamenRequest;
import com.admision.exception.BancoPreguntasInsuficienteException;
import com.admision.service.GeneradorExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/examenes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExamenGeneradoController {

    private final GeneradorExamenService generadorExamenService;

    @PostMapping("/generar")
    public ResponseEntity<ExamenGeneradoResponse> generarExamen(
            @RequestBody GenerarExamenRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(generadorExamenService.generarExamen(request));
    }

    @GetMapping
    public ResponseEntity<List<ExamenGeneradoResponse>> listarExamenes() {
        return ResponseEntity.ok(
                generadorExamenService.listarExamenes()
        );
    }

    @GetMapping("/{examenId}")
    public ResponseEntity<ExamenGeneradoResponse> obtenerExamen(
            @PathVariable Long examenId
    ) {
        return ResponseEntity.ok(
                generadorExamenService.obtenerExamen(examenId)
        );
    }

    @GetMapping("/{examenId}/base")
    public ResponseEntity<DetalleExamenBaseResponse> obtenerExamenBase(
            @PathVariable Long examenId
    ) {
        return ResponseEntity.ok(
                generadorExamenService.obtenerExamenBase(examenId)
        );
    }

    @ExceptionHandler(BancoPreguntasInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarBancoInsuficiente(
            BancoPreguntasInsuficienteException ex
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("error", true);
        response.put("mensaje", ex.getMessage());
        response.put("area", ex.getArea());
        response.put("faltantes", ex.getFaltantes());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            IllegalArgumentException ex
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("error", true);
        response.put("mensaje", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarEstadoInvalido(
            IllegalStateException ex
    ) {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("error", true);
        response.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(response);
    }
}