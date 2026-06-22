package com.admision.controller;

import com.admision.dto.tema.ClaveTemaResponse;
import com.admision.dto.tema.PreguntaTemaResponse;
import com.admision.dto.tema.TemaExamenResponse;
import com.admision.service.ChocolateoExamenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examenes/{examenId}/temas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TemaExamenController {

    private final ChocolateoExamenService chocolateoExamenService;

    @GetMapping
    public ResponseEntity<List<TemaExamenResponse>> listarTemas(
            @PathVariable Long examenId
    ) {
        return ResponseEntity.ok(
                chocolateoExamenService.listarTemas(examenId)
        );
    }

    @GetMapping("/{letraTema}")
    public ResponseEntity<TemaExamenResponse> obtenerTema(
            @PathVariable Long examenId,
            @PathVariable String letraTema
    ) {
        return ResponseEntity.ok(
                chocolateoExamenService.obtenerTema(
                        examenId,
                        letraTema
                )
        );
    }

    @GetMapping("/{letraTema}/preguntas")
    public ResponseEntity<List<PreguntaTemaResponse>> listarPreguntasTema(
            @PathVariable Long examenId,
            @PathVariable String letraTema
    ) {
        return ResponseEntity.ok(
                chocolateoExamenService.listarPreguntasTema(
                        examenId,
                        letraTema
                )
        );
    }

    @GetMapping("/{letraTema}/claves")
    public ResponseEntity<ClaveTemaResponse> obtenerClavesTema(
            @PathVariable Long examenId,
            @PathVariable String letraTema
    ) {
        return ResponseEntity.ok(
                chocolateoExamenService.obtenerClavesTema(
                        examenId,
                        letraTema
                )
        );
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
}