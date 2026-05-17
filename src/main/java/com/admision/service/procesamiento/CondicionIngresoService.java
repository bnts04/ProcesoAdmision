package com.admision.service.procesamiento;

import com.admision.dto.CondicionIngresoResponse;
import com.admision.entity.CarreraVacante;
import com.admision.entity.ProcesoAdmision;
import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import com.admision.enums.EstadoProceso;
import com.admision.repository.CarreraVacanteRepository;
import com.admision.repository.ProcesoAdmisionRepository;
import com.admision.repository.ResultadoPostulanteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CondicionIngresoService {

    private final ResultadoPostulanteRepository resultadoPostulanteRepository;
    private final CarreraVacanteRepository carreraVacanteRepository;
    private final ProcesoAdmisionRepository procesoAdmisionRepository;

    @Transactional
    public CondicionIngresoResponse calcularCondicionIngreso(Long procesoId) {
        ProcesoAdmision proceso = procesoAdmisionRepository.findById(procesoId)
                .orElseThrow(() -> new RuntimeException("Proceso de admisión no encontrado"));

        List<ResultadoPostulante> resultados = resultadoPostulanteRepository.findByProcesoId(procesoId);

        if (resultados.isEmpty()) {
            return CondicionIngresoResponse.builder()
                    .procesoId(procesoId)
                    .totalPostulantes(0)
                    .totalVacantesAplicadas(0)
                    .totalIngresantes(0)
                    .totalNoIngresantes(0)
                    .carrerasProcesadas(0)
                    .carrerasSinVacantes(0)
                    .ingresantesAdicionalesPorEmpate(0)
                    .carrerasSinVacantesDetalle(List.of())
                    .mensaje("No existen resultados para calcular condición de ingreso")
                    .build();
        }

        validarOrdenMeritoCalculado(resultados);

        List<CarreraVacante> vacantesActivas = carreraVacanteRepository.findByActivoTrueOrderByFacultadAscCarreraAsc();

        Map<String, CarreraVacante> vacantePorCarrera = vacantesActivas.stream()
                .collect(Collectors.toMap(
                        v -> normalizarTexto(v.getCarrera()),
                        v -> v,
                        (v1, v2) -> v1
                ));

        Map<String, List<ResultadoPostulante>> resultadosPorCarrera = resultados.stream()
                .collect(Collectors.groupingBy(r -> normalizarTexto(r.getCarrera())));

        int totalIngresantes = 0;
        int totalNoIngresantes = 0;
        int totalVacantesAplicadas = 0;
        int ingresantesAdicionalesPorEmpate = 0;

        List<String> carrerasSinVacantes = new ArrayList<>();

        for (Map.Entry<String, List<ResultadoPostulante>> entry : resultadosPorCarrera.entrySet()) {
            String carreraNormalizada = entry.getKey();
            List<ResultadoPostulante> grupoCarrera = entry.getValue();

            CarreraVacante vacante = vacantePorCarrera.get(carreraNormalizada);

            if (vacante == null) {
                carrerasSinVacantes.add(grupoCarrera.get(0).getCarrera());

                for (ResultadoPostulante resultado : grupoCarrera) {
                    resultado.setCondicion(CondicionPostulante.PENDIENTE);
                    resultado.setObservacion(agregarObservacion(
                            resultado.getObservacion(),
                            "No se encontraron vacantes configuradas para la carrera"
                    ));
                }

                continue;
            }

            int vacantes = vacante.getVacantes();
            totalVacantesAplicadas += vacantes;

            List<ResultadoPostulante> ordenados = grupoCarrera.stream()
                    .sorted(Comparator
                            .comparing(ResultadoPostulante::getPuntajeFinal, Comparator.reverseOrder())
                            .thenComparing(ResultadoPostulante::getCodigo))
                    .toList();

            BigDecimal puntajeCorte;

            if (ordenados.size() <= vacantes) {
                puntajeCorte = BigDecimal.ZERO;
            } else {
                puntajeCorte = ordenados.get(vacantes - 1).getPuntajeFinal();
            }

            int ingresantesCarrera = 0;

            for (ResultadoPostulante resultado : ordenados) {
                if (resultado.getPuntajeFinal().compareTo(puntajeCorte) >= 0) {
                    resultado.setCondicion(CondicionPostulante.INGRESO);
                    ingresantesCarrera++;
                } else {
                    resultado.setCondicion(CondicionPostulante.NO_INGRESO);
                }

                resultado.setObservacion(agregarObservacion(
                        resultado.getObservacion(),
                        "Condición de ingreso calculada correctamente"
                ));
            }

            totalIngresantes += ingresantesCarrera;
            totalNoIngresantes += ordenados.size() - ingresantesCarrera;

            if (ingresantesCarrera > vacantes) {
                ingresantesAdicionalesPorEmpate += ingresantesCarrera - vacantes;
            }
        }

        resultadoPostulanteRepository.saveAll(resultados);

        proceso.setTotalIngresantes(totalIngresantes);
        proceso.setTotalNoIngresantes(totalNoIngresantes);

        if (carrerasSinVacantes.isEmpty()) {
            proceso.setEstado(EstadoProceso.COMPLETADO);
        } else {
            proceso.setEstado(EstadoProceso.CON_ADVERTENCIAS);
        }

        procesoAdmisionRepository.save(proceso);

        return CondicionIngresoResponse.builder()
                .procesoId(procesoId)
                .totalPostulantes(resultados.size())
                .totalVacantesAplicadas(totalVacantesAplicadas)
                .totalIngresantes(totalIngresantes)
                .totalNoIngresantes(totalNoIngresantes)
                .carrerasProcesadas(resultadosPorCarrera.size() - carrerasSinVacantes.size())
                .carrerasSinVacantes(carrerasSinVacantes.size())
                .ingresantesAdicionalesPorEmpate(ingresantesAdicionalesPorEmpate)
                .carrerasSinVacantesDetalle(carrerasSinVacantes)
                .mensaje(carrerasSinVacantes.isEmpty()
                        ? "Condición de ingreso calculada correctamente"
                        : "Condición calculada con advertencias: existen carreras sin vacantes configuradas")
                .build();
    }

    private void validarOrdenMeritoCalculado(List<ResultadoPostulante> resultados) {
        boolean faltaOrden = resultados.stream()
                .anyMatch(r -> r.getOmg() == null || r.getOme() == null);

        if (faltaOrden) {
            throw new RuntimeException("Primero debe calcular el orden de mérito general y por carrera");
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

    private String agregarObservacion(String observacionActual, String nuevaObservacion) {
        if (observacionActual == null || observacionActual.isBlank()) {
            return nuevaObservacion;
        }

        if (observacionActual.contains(nuevaObservacion)) {
            return observacionActual;
        }

        return observacionActual + " | " + nuevaObservacion;
    }
}