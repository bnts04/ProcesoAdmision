package com.admision.service;

import com.admision.dto.CarreraVacanteResponse;
import com.admision.dto.CrearCarreraVacanteRequest;
import com.admision.entity.CarreraVacante;
import com.admision.repository.CarreraVacanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarreraVacanteService {

    private final CarreraVacanteRepository carreraVacanteRepository;

    public CarreraVacanteResponse crear(CrearCarreraVacanteRequest request) {
        CarreraVacante carreraVacante = CarreraVacante.builder()
                .facultad(request.getFacultad().trim())
                .carrera(request.getCarrera().trim())
                .vacantes(request.getVacantes())
                .activo(true)
                .build();

        CarreraVacante guardado = carreraVacanteRepository.save(carreraVacante);

        return CarreraVacanteResponse.fromEntity(guardado);
    }

    public List<CarreraVacanteResponse> listarActivas() {
        return carreraVacanteRepository.findByActivoTrueOrderByFacultadAscCarreraAsc()
                .stream()
                .map(CarreraVacanteResponse::fromEntity)
                .toList();
    }

    public CarreraVacanteResponse obtenerPorId(Long id) {
        CarreraVacante carreraVacante = carreraVacanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera o vacante no encontrada"));

        return CarreraVacanteResponse.fromEntity(carreraVacante);
    }

    public void desactivar(Long id) {
        CarreraVacante carreraVacante = carreraVacanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrera o vacante no encontrada"));

        carreraVacante.setActivo(false);
        carreraVacanteRepository.save(carreraVacante);
    }

    public List<CarreraVacanteResponse> crearMasivo(List<CrearCarreraVacanteRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Debe enviar al menos una carrera con vacantes");
        }

        return requests.stream()
                .map(this::crearOActualizar)
                .toList();
    }

    private CarreraVacanteResponse crearOActualizar(CrearCarreraVacanteRequest request) {
        CarreraVacante carreraVacante = carreraVacanteRepository
                .findByCarreraIgnoreCaseAndActivoTrue(request.getCarrera().trim())
                .orElse(null);

        if (carreraVacante == null) {
            carreraVacante = CarreraVacante.builder()
                    .facultad(request.getFacultad().trim())
                    .carrera(request.getCarrera().trim())
                    .vacantes(request.getVacantes())
                    .activo(true)
                    .build();
        } else {
            carreraVacante.setFacultad(request.getFacultad().trim());
            carreraVacante.setVacantes(request.getVacantes());
        }

        CarreraVacante guardado = carreraVacanteRepository.save(carreraVacante);

        return CarreraVacanteResponse.fromEntity(guardado);
    }
}