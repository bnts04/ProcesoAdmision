package com.admision.controller;

import com.admision.dto.LecturaPdfGuiaResponse;
import com.admision.service.pdf.PdfGuiaLecturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.admision.dto.ActualizacionDesdePdfResponse;
import com.admision.service.pdf.ActualizacionDesdePdfService;

@RestController
@RequestMapping("/api/pdf-guia")
@RequiredArgsConstructor
public class PdfGuiaController {

    private final PdfGuiaLecturaService pdfGuiaLecturaService;
    private final ActualizacionDesdePdfService actualizacionDesdePdfService;

    @GetMapping("/proceso/{procesoId}/leer")
    public LecturaPdfGuiaResponse leerPdfGuia(
            @PathVariable Long procesoId,
            @RequestParam(defaultValue = "20") Integer limite
    ) {
        return pdfGuiaLecturaService.leerPdfGuia(procesoId, limite);
    }

    @PostMapping("/proceso/{procesoId}/actualizar-resultados")
    public ActualizacionDesdePdfResponse actualizarResultadosDesdePdf(@PathVariable Long procesoId) {
        return actualizacionDesdePdfService.actualizarDatosDesdePdf(procesoId);
    }
}