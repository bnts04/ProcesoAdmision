package com.admision.controller;

import com.admision.dto.ValidacionFuentesResponse;
import com.admision.service.validacion.ValidacionFuentesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/validaciones")
@RequiredArgsConstructor
public class ValidacionFuentesController {

    private final ValidacionFuentesService validacionFuentesService;

    @GetMapping("/proceso/{procesoId}/fuentes")
    public ValidacionFuentesResponse validarFuentes(@PathVariable Long procesoId) {
        return validacionFuentesService.validarFuentes(procesoId);
    }

    @PostMapping("/proceso/{procesoId}/fuentes/aplicar")
    public ValidacionFuentesResponse validarFuentesYActualizarEstado(@PathVariable Long procesoId) {
        return validacionFuentesService.validarFuentesYActualizarEstado(procesoId);
    }
}