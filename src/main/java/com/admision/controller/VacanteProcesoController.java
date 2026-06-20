package com.admision.controller;

import com.admision.dto.ActualizarVacanteProcesoRequest;
import com.admision.dto.VacanteProcesoResponse;
import com.admision.service.VacanteProcesoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procesos/{procesoId}/vacantes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VacanteProcesoController {

    private final VacanteProcesoService vacanteProcesoService;

    @GetMapping
    public List<VacanteProcesoResponse> listarVacantesProceso(@PathVariable Long procesoId) {
        return vacanteProcesoService.listarOInicializar(procesoId);
    }

    @PutMapping
    public List<VacanteProcesoResponse> actualizarVacantesProceso(
            @PathVariable Long procesoId,
            @Valid @RequestBody List<ActualizarVacanteProcesoRequest> requests
    ) {
        return vacanteProcesoService.actualizarVacantes(procesoId, requests);
    }
}