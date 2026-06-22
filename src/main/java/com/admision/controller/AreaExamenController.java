package com.admision.controller;

import com.admision.dto.area.AreaExamenResponse;
import com.admision.dto.area.DistribucionAreaExamenResponse;
import com.admision.service.ConfiguracionAreaExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/areas-examen")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AreaExamenController {

    private final ConfiguracionAreaExamenService configuracionAreaExamenService;

    @GetMapping
    public ResponseEntity<List<AreaExamenResponse>> listarAreas() {
        return ResponseEntity.ok(configuracionAreaExamenService.listarAreas());
    }

    @GetMapping("/{codigoArea}/configuracion")
    public ResponseEntity<DistribucionAreaExamenResponse> obtenerConfiguracionArea(
            @PathVariable String codigoArea
    ) {
        return ResponseEntity.ok(configuracionAreaExamenService.obtenerConfiguracionArea(codigoArea));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorValidacion(IllegalArgumentException ex) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", true);
        response.put("mensaje", ex.getMessage());

        return ResponseEntity.badRequest().body(response);
    }
}