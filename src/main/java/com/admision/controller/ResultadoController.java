package com.admision.controller;

import com.admision.dto.ResultadoPostulanteVistaResponse;
import com.admision.service.procesamiento.ResultadoConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.admision.dto.EstadisticaProcesoResponse;
import com.admision.service.procesamiento.EstadisticaProcesoService;
import com.admision.dto.ResumenCarreraResponse;

import java.util.List;

@RestController
@RequestMapping("/api/resultados")
@RequiredArgsConstructor
public class ResultadoController {

    private final ResultadoConsultaService resultadoConsultaService;
    private final EstadisticaProcesoService estadisticaProcesoService;

    @GetMapping("/proceso/{procesoId}")
    public List<ResultadoPostulanteVistaResponse> listarResultadosPorProceso(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "50") Integer limite
    ) {
        return resultadoConsultaService.listarResultadosPorProceso(procesoId, limite);
    }

    @GetMapping("/proceso/{procesoId}/estadisticas")
    public EstadisticaProcesoResponse obtenerEstadisticas(@PathVariable Long procesoId) {
        return estadisticaProcesoService.obtenerEstadisticas(procesoId);
    }

    @GetMapping("/proceso/{procesoId}/carrera")
    public List<ResultadoPostulanteVistaResponse> listarResultadosPorCarrera(
            @PathVariable Long procesoId,
            @RequestParam String nombre,
            @RequestParam(defaultValue = "50") Integer limite
    ) {
        return resultadoConsultaService.listarResultadosPorCarrera(procesoId, nombre, limite);
    }

    @GetMapping("/proceso/{procesoId}/resumen-carreras")
    public List<ResumenCarreraResponse> obtenerResumenPorCarreras(@PathVariable Long procesoId) {
        return resultadoConsultaService.obtenerResumenPorCarreras(procesoId);
    }
}