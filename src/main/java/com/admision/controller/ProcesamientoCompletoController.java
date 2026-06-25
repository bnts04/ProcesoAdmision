package com.admision.controller;

import com.admision.dto.ProcesamientoCompletoResponse;
import com.admision.service.procesamiento.ProcesamientoCompletoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/procesamiento")
@RequiredArgsConstructor
public class ProcesamientoCompletoController {

    private final ProcesamientoCompletoService procesamientoCompletoService;

    @PostMapping("/proceso/{procesoId}/ejecutar-todo")
    public ProcesamientoCompletoResponse ejecutarTodo(@PathVariable Long procesoId) {
        return procesamientoCompletoService.ejecutarTodo(procesoId);
    }
}