package com.admision.controller;

import com.admision.dto.ClaveTemaResponse;
import com.admision.service.excel.ClavesExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.admision.dto.RespuestaPostulanteExcelResponse;
import com.admision.service.excel.RespuestasExcelService;
import com.admision.dto.IdentificacionPostulanteExcelResponse;
import com.admision.service.excel.IdentificacionExcelService;
import com.admision.dto.PadronCarreraExcelResponse;
import com.admision.service.excel.PadronCarrerasExcelService;

import java.util.List;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ClavesExcelService clavesExcelService;
    private final RespuestasExcelService respuestasExcelService;
    private final IdentificacionExcelService identificacionExcelService;
    private final PadronCarrerasExcelService padronCarrerasExcelService;

    @GetMapping("/claves/proceso/{procesoId}")
    public List<ClaveTemaResponse> leerClaves(@PathVariable Long procesoId) {
        return clavesExcelService.leerClaves(procesoId);
    }

    @GetMapping("/respuestas/proceso/{procesoId}")
    public List<RespuestaPostulanteExcelResponse> leerRespuestas(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "20") Integer limite
    ) {
        return respuestasExcelService.leerRespuestas(procesoId, limite);
    }

    @GetMapping("/identificaciones/proceso/{procesoId}")
    public List<IdentificacionPostulanteExcelResponse> leerIdentificaciones(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "20") Integer limite
    ) {
        return identificacionExcelService.leerIdentificaciones(procesoId, limite);
    }

    @GetMapping("/padron-carreras/proceso/{procesoId}")
    public List<PadronCarreraExcelResponse> leerPadronCarreras(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "20") Integer limite
    ) {
        return padronCarrerasExcelService.leerPadronCarreras(procesoId, limite);
    }
}