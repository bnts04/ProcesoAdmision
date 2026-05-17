package com.admision.controller;

import com.admision.dto.CarreraVacanteResponse;
import com.admision.dto.CrearCarreraVacanteRequest;
import com.admision.service.CarreraVacanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacantes")
@RequiredArgsConstructor
public class CarreraVacanteController {

    private final CarreraVacanteService carreraVacanteService;

    @PostMapping
    public CarreraVacanteResponse crear(@Valid @RequestBody CrearCarreraVacanteRequest request) {
        return carreraVacanteService.crear(request);
    }

    @GetMapping
    public List<CarreraVacanteResponse> listarActivas() {
        return carreraVacanteService.listarActivas();
    }

    @GetMapping("/{id}")
    public CarreraVacanteResponse obtenerPorId(@PathVariable Long id) {
        return carreraVacanteService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void desactivar(@PathVariable Long id) {
        carreraVacanteService.desactivar(id);
    }
    @PostMapping("/masivo")
    public List<CarreraVacanteResponse> crearMasivo(
            @Valid @RequestBody List<CrearCarreraVacanteRequest> requests
    ) {
        return carreraVacanteService.crearMasivo(requests);
    }
}