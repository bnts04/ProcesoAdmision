package com.admision.controller;

import com.admision.dto.ActualizarCalificacionRequest;
import com.admision.dto.ConfiguracionCalificacionResponse;
import com.admision.dto.CrearProcesoRequest;
import com.admision.entity.ProcesoAdmision;
import com.admision.service.ProcesoAdmisionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProcesoAdmisionController {

    private final ProcesoAdmisionService procesoAdmisionService;

    @PostMapping
    public ProcesoAdmision crearProceso(@Valid @RequestBody CrearProcesoRequest request) {
        return procesoAdmisionService.crearProceso(request);
    }

    @GetMapping
    public List<ProcesoAdmision> listarProcesos() {
        return procesoAdmisionService.listarProcesos();
    }

    @GetMapping("/{id}")
    public ProcesoAdmision obtenerProceso(@PathVariable Long id) {
        return procesoAdmisionService.obtenerProcesoPorId(id);
    }

    @GetMapping("/{id}/configuracion-calificacion")
    public ConfiguracionCalificacionResponse obtenerConfiguracionCalificacion(@PathVariable Long id) {
        return procesoAdmisionService.obtenerConfiguracionCalificacion(id);
    }

    @PutMapping("/{id}/configuracion-calificacion")
    public ConfiguracionCalificacionResponse actualizarConfiguracionCalificacion(
            @PathVariable Long id,
            @RequestBody ActualizarCalificacionRequest request
    ) {
        return procesoAdmisionService.actualizarConfiguracionCalificacion(id, request);
    }
}