package com.admision.controller;

import com.admision.dto.VistaPreviaFinalResponse;
import com.admision.service.procesamiento.VistaPreviaFinalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procesamiento")
@RequiredArgsConstructor
public class VistaPreviaFinalController {

    private final VistaPreviaFinalService vistaPreviaFinalService;

    @GetMapping("/proceso/{procesoId}/vista-previa-final")
    public VistaPreviaFinalResponse obtenerVistaPreviaFinal(@PathVariable Long procesoId) {
        return vistaPreviaFinalService.obtenerVistaPreviaFinal(procesoId);
    }
}