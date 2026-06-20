package com.admision.service;

import com.admision.dto.ActualizarVacanteProcesoRequest;
import com.admision.dto.VacanteProcesoResponse;
import com.admision.entity.CarreraVacante;
import com.admision.entity.ProcesoAdmision;
import com.admision.entity.VacanteProceso;
import com.admision.repository.CarreraVacanteRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.VacanteProcesoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacanteProcesoService {

    private final VacanteProcesoRepository vacanteProcesoRepository;
    private final CarreraVacanteRepository carreraVacanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    @Transactional
    public List<VacanteProcesoResponse> listarOInicializar(Long procesoId) {
        ProcesoAdmision proceso = obtenerProceso(procesoId);

        List<VacanteProceso> existentes = vacanteProcesoRepository
                .findByProcesoIdOrderByFacultadAscCarreraAsc(procesoId);

        if (!existentes.isEmpty()) {
            return existentes.stream()
                    .map(VacanteProcesoResponse::fromEntity)
                    .toList();
        }

        List<CarreraVacante> vacantesGlobales = carreraVacanteRepository
                .findByActivoTrueOrderByFacultadAscCarreraAsc();

        if (vacantesGlobales.isEmpty()) {
            throw new RuntimeException("No existen vacantes globales configuradas.");
        }

        List<VacanteProceso> nuevasVacantes = vacantesGlobales.stream()
                .map(vacanteGlobal -> VacanteProceso.builder()
                        .proceso(proceso)
                        .facultad(vacanteGlobal.getFacultad())
                        .carrera(vacanteGlobal.getCarrera())
                        .vacantes(vacanteGlobal.getVacantes())
                        .build())
                .toList();

        List<VacanteProceso> guardadas = vacanteProcesoRepository.saveAll(nuevasVacantes);

        return guardadas.stream()
                .map(VacanteProcesoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public List<VacanteProcesoResponse> actualizarVacantes(
            Long procesoId,
            List<ActualizarVacanteProcesoRequest> requests
    ) {
        ProcesoAdmision proceso = obtenerProceso(procesoId);

        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Debe enviar al menos una carrera con vacantes.");
        }

        List<VacanteProceso> existentes = vacanteProcesoRepository
                .findByProcesoIdOrderByFacultadAscCarreraAsc(procesoId);

        Map<String, VacanteProceso> mapaExistentes = existentes.stream()
                .collect(Collectors.toMap(
                        v -> normalizarTexto(v.getCarrera()),
                        v -> v,
                        (v1, v2) -> v1
                ));

        for (ActualizarVacanteProcesoRequest request : requests) {
            validarRequest(request);

            String carreraNormalizada = normalizarTexto(request.getCarrera());

            VacanteProceso vacanteProceso = mapaExistentes.get(carreraNormalizada);

            if (vacanteProceso == null) {
                vacanteProceso = VacanteProceso.builder()
                        .proceso(proceso)
                        .facultad(request.getFacultad().trim())
                        .carrera(request.getCarrera().trim())
                        .vacantes(request.getVacantes())
                        .build();
            } else {
                vacanteProceso.setFacultad(request.getFacultad().trim());
                vacanteProceso.setCarrera(request.getCarrera().trim());
                vacanteProceso.setVacantes(request.getVacantes());
            }

            vacanteProcesoRepository.save(vacanteProceso);
        }

        return vacanteProcesoRepository
                .findByProcesoIdOrderByFacultadAscCarreraAsc(procesoId)
                .stream()
                .map(VacanteProcesoResponse::fromEntity)
                .toList();
    }

    private ProcesoAdmision obtenerProceso(Long procesoId) {
        return procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado."));
    }

    private void validarRequest(ActualizarVacanteProcesoRequest request) {
        if (request.getFacultad() == null || request.getFacultad().isBlank()) {
            throw new RuntimeException("La facultad es obligatoria.");
        }

        if (request.getCarrera() == null || request.getCarrera().isBlank()) {
            throw new RuntimeException("La carrera es obligatoria.");
        }

        if (request.getVacantes() == null || request.getVacantes() <= 0) {
            throw new RuntimeException("Las vacantes deben ser mayores a 0 para la carrera: " + request.getCarrera());
        }
    }

    private String normalizarTexto(String valor) {
        if (valor == null) {
            return "";
        }

        String limpio = valor.trim().toUpperCase();

        limpio = Normalizer.normalize(limpio, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        limpio = limpio.replaceAll("\\s+", " ");

        return limpio;
    }
}