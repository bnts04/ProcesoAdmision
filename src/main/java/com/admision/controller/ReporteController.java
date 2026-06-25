package com.admision.controller;

import com.admision.dto.ReporteTecnicoResponse;
import com.admision.service.reporte.ReporteTecnicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteTecnicoService reporteTecnicoService;

    @GetMapping("/proceso/{procesoId}/tecnico")
    public ReporteTecnicoResponse generarReporteTecnico(@PathVariable Long procesoId) {
        return reporteTecnicoService.generarReporteTecnico(procesoId);
    }
}