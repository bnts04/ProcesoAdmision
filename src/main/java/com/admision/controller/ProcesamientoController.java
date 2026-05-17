package com.admision.controller;

import com.admision.dto.PostulanteConRespuestasResponse;
import com.admision.service.procesamiento.CruceIdentificacionRespuestasService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.admision.dto.PuntajePostulanteResponse;
import com.admision.service.procesamiento.CalculoPuntajeService;
import com.admision.dto.ResultadoGuardadoResponse;
import com.admision.service.procesamiento.GuardarResultadosService;
import com.admision.dto.AsignacionCarrerasResponse;
import com.admision.service.procesamiento.AsignacionCarrerasService;
import com.admision.dto.OrdenMeritoResponse;
import com.admision.service.procesamiento.OrdenMeritoService;
import com.admision.dto.CondicionIngresoResponse;
import com.admision.service.procesamiento.CondicionIngresoService;

import java.util.List;

@RestController
@RequestMapping("/api/procesamiento")
@RequiredArgsConstructor
public class ProcesamientoController {

    private final CruceIdentificacionRespuestasService cruceIdentificacionRespuestasService;
    private final CalculoPuntajeService calculoPuntajeService;
    private final GuardarResultadosService guardarResultadosService;
    private final AsignacionCarrerasService asignacionCarrerasService;
    private final OrdenMeritoService ordenMeritoService;
    private final CondicionIngresoService condicionIngresoService;

    @GetMapping("/cruce-respuestas-identificacion/{procesoId}")
    public List<PostulanteConRespuestasResponse> cruzarRespuestasConIdentificacion(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "10") Integer limite
    ) {
        return cruceIdentificacionRespuestasService
                .cruzarRespuestasConIdentificacion(procesoId, limite);
    }

    @GetMapping("/calcular-puntajes/{procesoId}")
    public List<PuntajePostulanteResponse> calcularPuntajes(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "10") Integer limite
    ) {
        return calculoPuntajeService.calcularPuntajes(procesoId, limite);
    }

    @PostMapping("/guardar-puntajes/{procesoId}")
    public ResultadoGuardadoResponse guardarPuntajes(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "10") Integer limite
    ) {
        return guardarResultadosService.guardarResultadosCalculados(procesoId, limite);
    }

    @PostMapping("/asignar-carreras/{procesoId}")
    public AsignacionCarrerasResponse asignarCarrerasYFacultades(@PathVariable Long procesoId) {
        return asignacionCarrerasService.asignarCarrerasYFacultades(procesoId);
    }

    @PostMapping("/calcular-orden-merito/{procesoId}")
    public OrdenMeritoResponse calcularOrdenMerito(@PathVariable Long procesoId) {
        return ordenMeritoService.calcularOrdenMerito(procesoId);
    }

    @PostMapping("/calcular-condicion/{procesoId}")
    public CondicionIngresoResponse calcularCondicionIngreso(@PathVariable Long procesoId) {
        return condicionIngresoService.calcularCondicionIngreso(procesoId);
    }
}